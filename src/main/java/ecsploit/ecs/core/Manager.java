package ecsploit.ecs.core;

public class Manager {
	
	private final EntityManager entityManager;
	private final ComponentManager componentManager;
	private final SystemManager systemManager;
	
	public Manager() {
		this.entityManager = new EntityManager(this);
		this.componentManager = new ComponentManager(this);
		this.systemManager = new SystemManager(this);
	}

	//ENTITY MANAGER WRAPPER FUNCTIONS//

	EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * Get Entity instance for given unique id
	 * <p>
	 *     Note: there is only ever one entity instance per id. Subsequent "get" calls return the same object.
	 * </p>
	 *
	 * @param entityID unique int identifier of the entity to be queried
	 * @return an Entity instance with given id
	 */
	public Entity get(int entityID) {
		return entityManager.getEntity(entityID);
	}

	/**
	 * @return the total number of active entities
	 */
	public int sizeOfEntities() {
		return entityManager.getEntitiesSize();
	}

	/**
	 * Creates a new entity. If an entity was recently destroyed, it will try to reuse the destroyed entity object but
	 * a unique id is always guaranteed.
	 * @return a new entity instance with unique id
	 */
	public Entity entity() {
		return entityManager.getEntity(entityManager.createEntityID());
	}

	/**
	 * See {@link #entity() entity} method for more details.
	 * @return entity id of a newly created entity
	 */
	public int entityID() {
		return entityManager.createEntityID();
	}

	/**
	 * Destroys an entity and detaches related components. May or may not update entity groups depending on if the
	 * SystemManager allows it.
	 * <p>
	 *     Note: Entity object is reused for newly created entities in the future. DO NOT maintain reference/copy of
	 *     the entity object once entity has been destroyed. Doing so can lead to undefined behavior.
	 * </p>
	 * @param entity instance of entity to be destroyed
	 */
	public void destroy(Entity entity) {
		if (entity == null) throw new IllegalArgumentException("Entity argument must be non-null");
		entityManager.destroyEntity(entity.getID());
	}

	/**
	 * See {@link #destroy(Entity) destroy} method for more details.
	 * @param entityID id of entity to be destroyed
	 */
	public void destroyID(int entityID) {
		if (entityID < 0) throw new IllegalArgumentException("EntityID argument must be non-negative");
		entityManager.destroyEntity(entityID);
	}

	/**
	 * Generates a console message which summarizes currently active entities.
	 * @return string representation of the EntityManager
	 */
	public String viewEntities() {
		return entityManager.toString();
	}

	//COMPONENT MANAGER WRAPPER FUNCTIONS//

	ComponentManager getComponentManager() {
		return componentManager;
	}

	/**
	 * Gets the ComponentType object for the componentClass queried. Guaranteed to return a non-null componentType.
	 *
	 * @param componentClass class of a component to get the type of
	 * @param <T> type of component
	 * @return componentType related to the componentClass
	 */
	public <T extends Component> ComponentType<T> type(Class<T> componentClass) {
		return componentManager.getComponentType(componentClass);
	}

	/**
	 * Creates Component instance from component class and attempts to attach it to the entity.
	 * <p>
	 *     Notes:
	 *     <ul>
	 *         <li>Slower than attachT method with cached componentType as parameter</li>
	 *         <li>May or may not notify attach observers depending on whether or not the SystemManager allows it</li>
	 *     </ul>
	 * </p>
	 *
	 * @param entity the entity to which the component will be attached to
	 * @param componentClass class of component to add to the entity
	 * @param <T> type of the component
	 * @return Component instance attached to the entity
	 */
	public <T extends Component> T attach(Entity entity, Class<T> componentClass) {
		return componentManager.attach(entity.getID(), componentClass);
	}

	public <T extends Component> T attach(int entityID, Class<T> componentClass) {
		return componentManager.attach(entityID, componentClass);
	}

	/**
	 * Similar to regular attach except it takes in a cached ComponentType to avoid map lookup. Faster than regular
	 * attach but requires componentType object to cached.
	 *
	 * @param entity the entity to which the component will be attached to
	 * @param componentType type of component to add to the entity
	 * @param <T> type of the component
	 * @return Component instance attached to the entity
	 */
	public <T extends Component> T attachT(Entity entity, ComponentType<T> componentType) {
		return componentManager.attachT(entity.getID(), componentType);
	}

	public <T extends Component> T attachT(int entityID, ComponentType<T> componentType) {
		return componentManager.attachT(entityID, componentType);
	}

	/**
	 * Attempts to remove ComponentType from the entity.
	 * <p>
	 *     Notes:
	 *     <ul>
	 *         <li>Slower than detachT method with cached componentType as parameter</li>
	 *         <li>May or may not notify attach observers depending on whether or not the SystemManager allows it</li>
	 *     </ul>
	 * </p>
	 *
	 * @param entity the entity to which the component will be detached to
	 * @param componentClass class of component to remove from the entity
	 * @param <T> type of the component
	 * @return Component instance detached to the entity or NULL if entity does not contain the ComponentType
	 */
	public <T extends Component> T detach(Entity entity, Class<T> componentClass) {
		return componentManager.detach(entity.getID(), componentClass);
	}

	public <T extends Component> T detach(int entityID, Class<T> componentClass) {
		return componentManager.detach(entityID, componentClass);
	}

	/**
	 * Similar to regular detach except it takes in a cached ComponentType to avoid map lookup. Faster than regular
	 * detach but requires componentType object to cached.
	 *
	 * @param entity the entity to which the component will be detached to
	 * @param componentType type of component to remove from the entity
	 * @param <T> type of the component
	 * @return Component instance detached to the entity or NULL if entity does not contain the ComponentType
	 */
	public <T extends Component> T detachT(Entity entity, ComponentType<T> componentType) {
		return componentManager.detachT(entity.getID(), componentType);
	}

	public <T extends Component> T detachT(int entityID, ComponentType<T> componentType) {
		return componentManager.detachT(entityID, componentType);
	}

	/**
	 * Safely check whether an entityID has a given component instance of ComponentType. Will not throw errors.
	 *
	 * @param entity entity to be queried
	 * @param componentType type of component to be queried
	 * @return whether or not the entity contains an instance of the ComponentType
	 */
	public boolean has(Entity entity, ComponentType<? extends Component> componentType) {
		return componentManager.has(entity.getID(), componentType);
	}

	/**
	 * Gets a Category object which keeps an up-to-date list of all entities with the corresponding component types.
	 * <p>
	 *     Note: It is assumed that component types in the list are unique. Undefined behavior can arise if the same
	 *     component type is listed repeatedly.
	 * </p>
	 *
	 * @param componentClasses list of component classes to query
	 * @return real-time updated Category reference
	 */
	@SafeVarargs
	public final Category category(Class<? extends Component>... componentClasses) {
		return componentManager.getCategory(componentClasses);
	}

	/**
	 * Faster method of generating/finding a category with relevant components. See {@link #category(Class[]) group}
	 * for more details.
	 *
	 * @param componentTypes list of component types to query
	 * @return real-time updated EntityGroup reference
	 */
	@SafeVarargs
	public final Category categoryT(ComponentType<? extends Component>... componentTypes) {
		return componentManager.getCategoryT(componentTypes);
	}

	//SYSTEM MANAGER WRAPPER FUNCTIONS//

	SystemManager getSystemManager() {
		return systemManager;
	}

	/**
	 * Registers the system into the system. Also injects the system with appropriate dependencies.
	 * @param system instance of system to be registered
	 */
	public void system(AbstractSystem system) {
		systemManager.register(system);
	}

	/**
	 * Generates a console message which summarizes currently active systems.
	 * @return string representation of the SystemManager
	 */
	public String viewSystems() {
		return systemManager.toString();
	}

	public void update() {
		componentManager.setToDeferredStrategy();
		systemManager.update();
		componentManager.setToImmediateStrategy();

		componentManager.clean();
	}
}
