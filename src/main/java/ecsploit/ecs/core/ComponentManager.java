package ecsploit.ecs.core;

import ecsploit.utils.collections.BitString;
import ecsploit.utils.collections.DenseList;
import ecsploit.utils.collections.DenseQueue;

import java.util.Arrays;

final class ComponentManager {
	
	//Map of componentClass to respective componentType
	private final ComponentTypeMap componentTypeMap = new ComponentTypeMap();
	
	//Map from entityID to respective component bits
	private BitString[] entityToComponentBits = new BitString[64];

	private final DenseList<BitString> categoryBitStrings = new DenseList<>(); //List of BitStrings in same order as categories
	private final DenseList<Category> categories = new DenseList<>(); //List of all entityGroups

	//Attach and Detach strategies
	private ComponentOperationStrategy attachStrategy;
	private ComponentOperationStrategy detachStrategy;

	//Deferred component operations
	private final DenseQueue<ComponentOperation> deferredAttaches = new DenseQueue<>();
	private final DenseQueue<ComponentOperation> deferredDetaches = new DenseQueue<>();
	
	private final Manager manager;
	
	ComponentManager(Manager manager) {
		this.manager = manager;

		this.setToImmediateStrategy();
	}

	void createComponentBitsInternal(int entityID) {
		if (entityID >= this.entityToComponentBits.length) this.entityToComponentBits = Arrays.copyOf(this.entityToComponentBits, entityID + (entityID >>> 1) + 1);
		entityToComponentBits[entityID] = new BitString();
	}

	void deleteComponentBitsInternal(int entityID) {
		BitString componentBits = this.entityToComponentBits[entityID];

		while(!componentBits.isEmpty()) {
			int index = componentBits.indexOfLSB();
			this.detachT(entityID, componentTypeMap.getFromID(index));
			componentBits.clear(index);
		}
		entityToComponentBits[entityID] = null;
	}

	<T extends Component> ComponentType<T> getComponentType(Class<T> componentClass) {
		return componentTypeMap.getComponentType(componentClass);
	}

	private void setComponentBit(int entityID, int componentID) {
		this.entityToComponentBits[entityID].set(componentID);
	}

	private void clearComponentBit(int entityID, int componentID) {
		this.entityToComponentBits[entityID].clear(componentID);
	}

	void setToImmediateStrategy() {
		this.attachStrategy = this.immediateAttachStrategy;
		this.detachStrategy = this.immediateDetachStrategy;
	}

	void setToDeferredStrategy() {
		this.attachStrategy = this.deferredAttachStrategy;
		this.detachStrategy = this.deferredDetachStrategy;
	}

	/**
	 * Creates Component instance, and immediately updates the ComponentType and notifies related EntityGroups of
	 * changes. Also notifies all the attach ComponentObservers.
	 */
	private final ComponentOperationStrategy immediateAttachStrategy = new ComponentOperationStrategy(this) {
		public <T extends Component> T invoke(int entityID, ComponentType<T> componentType) {
			T componentInstance = componentType.addInternalEntity(entityID);

			this.componentManager.setComponentBit(entityID, componentType.getID());
			componentType.notifyAttachObservers(entityID);
			return componentInstance;
		}
	};

	/**
	 * Called while current system is updating. Creates Component instance which can be modified and
	 * retrieved by user.
	 * <p>
	 *     Notes:
	 *     <ul>
	 *         <li>Component instance is immediately stored in the ComponentType</li>
	 *         <li>Component bits for entity are NOT updated immediately</li>
	 *         <li>EntityGroups are NOT updated as to avoid concurrent modifications</li>
	 *         <li>Attach ComponentObservers are NOT notified until end of current system update</li>
	 *     </ul>
	 * </p>
	 */
	private final ComponentOperationStrategy deferredAttachStrategy = new ComponentOperationStrategy(this) {
		public <T extends Component> T invoke(int entityID, ComponentType<T> componentType) {
			T componentInstance = componentType.addInternalEntity(entityID);
			this.componentManager.deferredAttaches.push(new ComponentOperation(entityID, componentType));
			return componentInstance;
		}
	};

	/**
	 * See {@link Manager#attach(Entity, Class) wrapper} for more details.
	 */
	<T extends Component> T attach(int entityID, Class<T> componentClass) {
		return this.attachStrategy.invoke(entityID, this.getComponentType(componentClass));
	}

	/**
	 * See {@link Manager#attachT(Entity, ComponentType) wrapper} for more details.
	 */
	<T extends Component> T attachT(int entityID, ComponentType<T> componentType) {
		return this.attachStrategy.invoke(entityID, componentType);
	}

	/**
	 * Detach Component instance, and immediately updates the ComponentType and notifies related EntityGroups of
	 * changes. Also notifies all the detach ComponentObservers.
	 * <p>
	 *     Note: Returns null if entity does not contain the componentType
	 * </p>
	 */
	private final ComponentOperationStrategy immediateDetachStrategy = new ComponentOperationStrategy(this) {
		public <T extends Component> T invoke(int entityID, ComponentType<T> componentType) {
			T componentInstance = componentType.removeInternalEntity(entityID);
			if (componentInstance == null) return null;

			this.componentManager.clearComponentBit(entityID, componentType.getID());
			componentType.notifyDetachObservers(entityID);
			return componentInstance;
		}
	};

	/**
	 * Called while current system is updating. Immediately adds change to deferredDetaches list.
	 * <p>
	 *     Notes:
	 *     <ul>
	 *         <li>Component bits for entity are NOT updated immediately</li>
	 *         <li>EntityGroups are NOT updated as to avoid concurrent modifications</li>
	 *         <li>Attach ComponentObservers are NOT notified until end of current system update</li>
	 *     </ul>
	 * </p>
	 */
	final ComponentOperationStrategy deferredDetachStrategy = new ComponentOperationStrategy(this) {
		public <T extends Component> T invoke(int entityID, ComponentType<T> componentType) {
			T componentInstance = componentType.removeInternalEntity(entityID);
			if (componentInstance == null) return null;
			this.componentManager.deferredDetaches.push(new ComponentOperation(entityID, componentType));
			return componentInstance;
		}
	};

	/**
	 * See {@link Manager#detach(Entity, Class) wrapper} for more details.
	 */
	<T extends Component> T detach(int entityID, Class<T> componentClass) {
		return this.detachStrategy.invoke(entityID, this.getComponentType(componentClass));
	}

	/**
	 * See {@link Manager#detachT(Entity, ComponentType) wrapper} for more details.
	 */
	<T extends Component> T detachT(int entityID, ComponentType<T> componentType) {
		return this.detachStrategy.invoke(entityID, componentType);
	}

	/**
	 * See {@link Manager#has(Entity, ComponentType) wrapper} for more details.
	 */
	boolean has(int entityID, ComponentType<? extends Component> componentType) {
		return componentType.contains(entityID);
	}

	/**
	 * See {@link Manager#category(Class[]) wrapper} for more details.
	 */
	@SafeVarargs
	final Category getCategory(Class<? extends Component>... componentClasses) {
		ComponentType<?>[] componentTypes = new ComponentType[componentClasses.length];
		for (int i = 0; i < componentClasses.length; i++) {
			componentTypes[i] = manager.getComponentManager().getComponentType(componentClasses[i]);
		}
		return this.getCategoryT(componentTypes);
	}

	/**
	 * See {@link Manager#categoryT(ComponentType[]) wrapper} for more details.
	 */
	@SafeVarargs
	final Category getCategoryT(ComponentType<? extends Component>... componentTypes) {
		BitString queriedComponents = new BitString();
		for (ComponentType<? extends Component> type: componentTypes) { //Generate bitString from componentType list
			queriedComponents.set(type.getID());
		}
		for (int i = 0; i < categories.size(); i++) {
			if (this.categoryBitStrings.fastGet(i).equals(queriedComponents)) { //Attempt to find cache of queried components
				return categories.fastGet(i);
			}
		}

		return createNewCategory(queriedComponents, componentTypes);
	}

	final Category createNewCategory(BitString queriedComponents, ComponentType<? extends Component>[] componentTypes) {
		Category category = new Category();
		manager.getEntityManager().forEach(entityID -> {
			if (this.entityToComponentBits[entityID].includes(queriedComponents)) {
				category.addInternalEntity(entityID);
			}
		});
		this.categories.add(category); //Cache the category for future retrieval

		for (ComponentType<? extends Component> type: componentTypes) { //Add dependency handlers to automatically manage category in the future
			type.onComponentAttach(entityID -> {
				if (!category.contains(entityID) && this.entityToComponentBits[entityID].includes(queriedComponents)) {
					category.addInternalEntity(entityID);
				}
			});
			type.onComponentDetach(entityID -> {
				if (category.contains(entityID)) category.removeInternalEntity(entityID);
			});
			type.onComponentChange(entityID -> {
				if (category.contains(entityID)) category.notifyChangeObservers(entityID);
			});
		}
		return category;
	}

	void clean() {
		int totalDeferredAttaches = deferredAttaches.size();
		for (int i = 0; i < totalDeferredAttaches; i++) {
			ComponentOperation attachOperation = deferredAttaches.poll();
			this.setComponentBit(attachOperation.entityID, attachOperation.componentType.getID());
			attachOperation.componentType.notifyAttachObservers(attachOperation.entityID);
		}
		this.deferredAttaches.reset();

		int totalDeferredDetaches = deferredDetaches.size();
		for (int i = 0; i < totalDeferredDetaches; i++) {
			ComponentOperation detachOperation = deferredDetaches.poll();
			this.clearComponentBit(detachOperation.entityID, detachOperation.componentType.getID());
			detachOperation.componentType.notifyDetachObservers(detachOperation.entityID);
		}
		this.deferredDetaches.reset();
	}

	private abstract static class ComponentOperationStrategy {

		final ComponentManager componentManager;

		public ComponentOperationStrategy(ComponentManager componentManager) {
			this.componentManager = componentManager;
		}

		abstract <T extends Component> T invoke(int entityID, ComponentType<T> componentType);

	}

	private static class ComponentOperation {

		final int entityID;
		final ComponentType<? extends Component> componentType;

		ComponentOperation(int entityID, ComponentType<? extends Component> componentType) {
			this.entityID = entityID;
			this.componentType = componentType;
		}
	}
}
