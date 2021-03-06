package ecsploit.ecs.core;


/**
 * A purely "ease-of-use" structure which compiles a unique id and manager to allow for more logical operations
 */
public final class Entity {
	
	private final int id;
	
	private final Manager manager;

	Entity(int id, Manager manager) {
		this.id = id;
		this.manager = manager;
	}

	public int getID() {
		return id;
	}

	/**
	 * Queries entity for whether or not it contains the given ComponentType.
	 * <p>
	 *     Note: This is a wrapper function for manager.getComponentManager().has(...);
	 * </p>
	 *
	 * @param componentType type of component to be queried
	 * @return whether or not the given entity has a component with given type
	 */
	public boolean has(ComponentType<? extends Component> componentType) {
		return manager.getComponentManager().has(id, componentType);
	}

	/**
	 * Gets the component instance belonging to this entity if present.
	 *
	 * @param componentType type of component to be queried
	 * @param <T> Component Type
	 * @return a component of given type if present or NULL if not present
	 */
	public <T extends Component> T get(ComponentType<T> componentType) {
		return componentType.retrieve(id);
	}

	/**
	 * Attach component to this entity. Wrapper for {@link ComponentManager#attach(int, Class) ComponentManager.attach(...)}
	 *
	 * @param componentClass class of component to be attached
	 * @param <T> Component Type
	 * @return component instance of type related to componentType
	 */
	public <T extends Component> T attach(Class<T> componentClass) {
		return manager.getComponentManager().attach(id, componentClass);
	}

	/**
	 * Wrapper function for attaching component to this entity when ComponentType is known.
	 *
	 * @param componentType type of component to be attached
	 * @param <T> Component Type
	 * @return component instance of type related to componentType
	 */
	public <T extends Component> T attachT(ComponentType<T> componentType) {
		return manager.getComponentManager().attachT(id, componentType);
	}

	/**
	 * Detach component to this entity. Wrapper for {@link ComponentManager#detach(int, Class)
	 * ComponentManager.detach(...)}
	 *
	 * @param componentClass class of component to be detached
	 * @param <T> Component Type
	 * @return component instance detached or NULL if entity does not have component type
	 */
	public <T extends Component> T detach(Class<T> componentClass) {
		return manager.getComponentManager().detach(id, componentClass);
	}

	/**
	 * Wrapper function for detaching component from this entity when ComponentType is known.
	 *
	 * @param componentType type of component to be detached
	 * @param <T> Component Type
	 * @return component instance detached or NULL if entity does not have component type
	 */
	public <T extends Component> T detachT(ComponentType<T> componentType) {
		return manager.getComponentManager().detachT(id, componentType);
	}

	public String toString() {
		return "Entity(id=" + id + ")";
	}

}
