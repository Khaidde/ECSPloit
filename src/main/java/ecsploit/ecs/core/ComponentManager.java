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

	//Attach and Detach strategies
	private ComponentOperationStrategy attachStrategy;
	private ComponentOperationStrategy detachStrategy;
	
	//Dealing with entity groups and all queried component bit combinations
	private final DenseList<EntityGroup> entityGroups = new DenseList<>(); //List of all entityGroups

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

	void deleteComponentBitsInternal(Entity entity) {
		BitString componentBits = this.entityToComponentBits[entity.getID()];

		while(!componentBits.isEmpty()) {
			int index = componentBits.indexOfLSB();
			this.detachT(entity, componentTypeMap.getFromID(index));
			componentBits.clear(index);
		}
		entityToComponentBits[entity.getID()] = null;
	}

	<T extends Component> ComponentType<T> getComponentType(Class<T> componentClass) {
		return componentTypeMap.getComponentType(componentClass);
	}

	private void setComponentFlag(int entityID, int componentID) {
		this.entityToComponentBits[entityID].set(componentID);
	}

	private void clearComponentFlag(int entityID, int componentID) {
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
			T componentInstance = componentType.attachInternalComponent(entityID);

			this.componentManager.setComponentFlag(entityID, componentType.getID());
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
			T componentInstance = componentType.attachInternalComponent(entityID);
			this.componentManager.deferredAttaches.push(new ComponentOperation(entityID, componentType));
			return componentInstance;
		}
	};

	/**
	 * See {@link Manager#attach(Entity, Class) wrapper} for more details.
	 */
	<T extends Component> T attach(Entity entity, Class<T> componentClass) {
		return this.attachStrategy.invoke(entity.getID(), this.getComponentType(componentClass));
	}

	/**
	 * See {@link Manager#attachT(Entity, ComponentType) wrapper} for more details.
	 */
	<T extends Component> T attachT(Entity entity, ComponentType<T> componentType) {
		return this.attachStrategy.invoke(entity.getID(), componentType);
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
			T componentInstance = componentType.detachInternalComponent(entityID);
			if (componentInstance == null) return null;

			this.componentManager.clearComponentFlag(entityID, componentType.getID());
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
			T componentInstance = componentType.detachInternalComponent(entityID);
			if (componentInstance == null) return null;
			this.componentManager.deferredDetaches.push(new ComponentOperation(entityID, componentType));
			return componentInstance;
		}
	};

	/**
	 * See {@link Manager#detach(Entity, Class) wrapper} for more details.
	 */
	<T extends Component> T detach(Entity entity, Class<T> componentClass) {
		return this.detachStrategy.invoke(entity.getID(), this.getComponentType(componentClass));
	}

	/**
	 * See {@link Manager#detachT(Entity, ComponentType) wrapper} for more details.
	 *
	 * @param entity the entity to which the component will be detached to
	 * @param componentType type of component to remove from the entity
	 * @param <T> type of the component
	 * @return Component instance detached to the entity or NULL if entity does not contain the ComponentType
	 */
	<T extends Component> T detachT(Entity entity, ComponentType<T> componentType) {
		return this.detachStrategy.invoke(entity.getID(), componentType);
	}

	/**
	 * See {@link Manager#has(Entity, ComponentType) wrapper} for more details.
	 */
	boolean has(int entityID, ComponentType<? extends Component> componentType) {
		return componentType.contains(entityID);
	}

	/**
	 * See {@link Manager#group(ComponentType[]) wrapper} for more details.
	 */
	@SafeVarargs
	final EntityGroup getGroup(ComponentType<? extends Component>... componentTypes) {
		BitString queriedComponents = new BitString();
		for (ComponentType<? extends Component> type: componentTypes) { //Generate bitString from componentType list
			queriedComponents.set(type.getID());
		}
		for (int i = 0; i < entityGroups.size(); i++) {
			EntityGroup group = entityGroups.fastGet(i);
			if (group.matches(queriedComponents)) { //Attempt to find cache of queried components
				return group;
			}
		}

		final BitString currentEntityGroupBits = new BitString(queriedComponents);
		EntityGroup entityGroup = new EntityGroup(currentEntityGroupBits);
		manager.getEntityManager().forEach(entityID -> {
			if (this.entityToComponentBits[entityID].includes(queriedComponents)) {
				entityGroup.addEntity(entityID);
			}
		});
		this.entityGroups.add(entityGroup); //Cache the entityGroup for future retrieval

		for (ComponentType<? extends Component> type: componentTypes) { //Add dependency handlers to automatically manage entity group in the future
			type.onComponentAttach((entityID, componentType) -> {
				if (!entityGroup.contains(entityID) && this.entityToComponentBits[entityID].includes(currentEntityGroupBits)) {
					entityGroup.addEntity(entityID);
				}
			});
			type.onComponentDetach((entityID, componentType) -> {
				if (entityGroup.contains(entityID)) entityGroup.removeEntity(entityID);
			});
		}

		return entityGroup;
	}

	void clean() {
		int totalDeferredAttaches = deferredAttaches.size();
		for (int i = 0; i < totalDeferredAttaches; i++) {
			ComponentOperation attachOperation = deferredAttaches.poll();
			this.setComponentFlag(attachOperation.entityID, attachOperation.componentType.getID());
			attachOperation.componentType.notifyAttachObservers(attachOperation.entityID);
		}
		this.deferredAttaches.reset();

		int totalDeferredDetaches = deferredDetaches.size();
		for (int i = 0; i < totalDeferredDetaches; i++) {
			ComponentOperation detachOperation = deferredDetaches.poll();
			this.clearComponentFlag(detachOperation.entityID, detachOperation.componentType.getID());
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
