package ecsploit.ecs.core;

import ecsploit.utils.collections.DenseList;

import java.util.function.Supplier;

public final class ComponentType<T extends Component> extends Category {

    private final Class<T> componentClass;
    private final int componentTypeID;

    private Supplier<T> componentConstructor;

    private final DenseList<T> componentInstances = new DenseList<>();

    ComponentType(Class<T> componentClass, int componentTypeID) {
        this.componentClass = componentClass;
        this.componentTypeID = componentTypeID;
    }

    public String getComponentName() {
        return this.componentClass.getSimpleName();
    }

    int getComponentID() {
        return componentTypeID;
    }

    /**
     * Can be used to modify how the componentType generates components of the given type. The default
     * implementation uses java reflection to create objects since there is no way at runtime to get a component's
     * constructor without knowing the class of the component. Supplying the component object constructor to this
     * component type can allow for faster component creation and thus faster component attachment.
     * <p>
     *     Example Usage:
     *     <pre>
     *         {@code
     *         class Position implements Component {}
     *         ComponentType<Position> positionComponentType = ...
     *         positionComponentType.registerConstructor(Position::new);
     *         }
     *     </pre>
     * </p>
     *
     * @param componentConstructor function while supplies the give component
     */
    public void registerConstructor(Supplier<T> componentConstructor) {
        this.componentConstructor = componentConstructor;
    }

    static class IllegalComponentAccessException extends RuntimeException {
        public IllegalComponentAccessException(String message) {
            super(message);
        }
    }

    /**
     * Safe retrieval of component from the ComponentType given an entity id.
     * @throws IllegalComponentAccessException when entityID is not attached to the associated ComponentType
     *
     * @param entityID id of entity
     * @return component related to the entity or NULL if entity does not contain the component.
     */
    public T retrieve(int entityID) {
        int index = this.entities.indexOf(entityID);
        if (index == -1) throw new IllegalComponentAccessException("Entity id=" + entityID + " does not contain ComponentType=" + this.getComponentName());
        return componentInstances.get(index);
    }

    void addInternalEntity(int entityID) {
        this.entities.add(entityID);
    }

    T addAndCreateInternalEntity(int entityID) {
        T componentInstance = this.componentConstructor.get();
        if (this.entities.contains(entityID)) {
            this.componentInstances.fastSet(this.entities.fastIndexOf(entityID), componentInstance);
        } else {
            this.addInternalEntity(entityID);
            this.componentInstances.add(componentInstance);
        }
        return componentInstance;
    }

    void removeInternalEntity(int entityID) {
        this.entities.fastRemove(entityID);
    }

    T removeAndGetInternalEntity(int entityID) {
        if (!this.has(entityID)) return null;

        int index = this.entities.indexOf(entityID);
        T object = this.componentInstances.fastRemove(index);
        this.removeInternalEntity(entityID);
        return object;
    }

    /**
     * @return entity stream which triggers on component attaches
     */
    public EntityStream attachStream() {
        return this.addStream;
    }

    /**
     * Assigns observer to be invoked whenever a component of this type is attached to an entity.
     * <p>
     *     Note: Observer won't be invoked immediately upon attach unless permitted by the SystemManager. This includes
     *     times in the pipeline where the user manually toggles the setting in the SystemManager or after each system
     * 	   update.
     *  </p>
     *
     * @param observer the function to be invoked when a component is attached
     * @return id of component observer added which can be later used to remove it
     */
    public int onComponentAttach(EntityObserver observer) {
        return this.addStream.connectObserver(observer);
    }

    /**
     * Disconnects an attach observer from listening to component attaches.
     *
     * @param attachObserverID unique id assigned to componentObserver when it was registered through the
     * {@link #onComponentAttach(EntityObserver) onComponentAttach} function.
     * @return instance of EntityObserver disconnected or NULL if ID was invalid
     */
    public EntityObserver disconnectAttachObserver(int attachObserverID) {
        return this.addStream.disconnectObserver(attachObserverID);
    }

    void notifyAttachObservers(int entityID) {
        this.addStream.notifyObservers(entityID);
    }

    /**
     * @return entity stream which triggers on component detaches
     */
    public EntityStream detachStream() {
        return this.removeStream;
    }

    /**
     * Assigns observer to be invoked whenever a component of this type is detached from an entity.
     * <p>
     *     Note: Observer won't be invoked immediately upon detach unless permitted by the SystemManager. This includes
     *     times in the pipeline where the user manually toggles the setting in the SystemManager or after each system
     * 	   update.
     *  </p>
     *
     * @param observer the function to be invoked when a component is detached
     * @return id of component observer added which can be later used to remove it
     */
    public int onComponentDetach(EntityObserver observer) {
        return this.removeStream.connectObserver(observer);
    }

    /**
     * Disconnects a detach observer from listening to component detaches.
     *
     * @param detachObserverID unique id assigned to EntityObserver when it was registered through the
     * {@link #onComponentDetach(EntityObserver) onComponentDetach} function.
     * @return instance of EntityObserver disconnected or NULL if ID was invalid
     */
    public EntityObserver disconnectDetachObserver(int detachObserverID) {
        return this.removeStream.disconnectObserver(detachObserverID);
    }

    void notifyDetachObservers(int entityID) {
        this.removeStream.notifyObservers(entityID);
    }

    /**
     * @return entity stream which triggers on component changes
     */
    public EntityStream changeStream() {
        return this.changeStream;
    }

    /**
     * Assigns observer to be invoked whenever a component of this type is changed.
     *
     * @param observer the function to be invoked when a component is changed
     * @return id of component observer added which can be later used to remove it
     */
    public int onComponentChange(EntityObserver observer) {
        return this.changeStream.connectObserver(observer);
    }

    /**
     * Disconnects a change observer from listening to component changes.
     *
     * @param changeObserverID unique id assigned to EventObserver when it was registered through the
     * {@link #onComponentChange(EntityObserver) onComponentChange} function.
     * @return instance of EntityObserver disconnected or NULL if ID was invalid
     */
    public EntityObserver disconnectChangeObserver(int changeObserverID) {
        return this.changeStream.disconnectObserver(changeObserverID);
    }

    /**
     * User should call this function to notify system of changes to a component. All "onComponentChange" callbacks will
     * be triggered.
     *
     * @param entityID id of entity whose component data has changed
     */
    public void notifyChangeObservers(int entityID) {
        super.notifyChangeObservers(entityID);
    }

    public String toString() {
        return "ComponentType(component=" + this.componentClass.getSimpleName() + ")";
    }
}
