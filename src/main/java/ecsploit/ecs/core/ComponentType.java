package ecsploit.ecs.core;

import ecsploit.utils.collections.PackedObjectList;

import java.util.function.Supplier;

public class ComponentType<T extends Component> {

    private final Class<T> componentClass;
    private final int id;

    private Supplier<T> componentConstructor;

    private final PackedObjectList<T> mappedComponentInstances = new PackedObjectList<>();

    private final PackedObjectList<ComponentObserver<T>> attachComponentObservers = new PackedObjectList<>();
    private final PackedObjectList<ComponentObserver<T>> detachComponentObservers = new PackedObjectList<>();
    private final PackedObjectList<ComponentObserver<T>> changeComponentObservers = new PackedObjectList<>();

    ComponentType(Class<T> componentClass, int id) {
        this.componentClass = componentClass;
        this.id = id;
    }

    int getID() {
        return id;
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

    boolean contains(int entityID) {
        return this.mappedComponentInstances.contains(entityID);
    }

    /**
     * Safe retrieval of component from the ComponentType given an entity id.
     *
     * @param entityID id of entity
     * @return component related to the entity or NULL if entity does not contain the component.
     */
    public T retrieve(int entityID) {
        return this.mappedComponentInstances.getObject(entityID);
    }

    /**
     * Assigns observer to be invoked whenever a component of this type is attached to an entity.
     * <p>
     *     Note: Observer won't be invoked immediately unless permitted by the SystemManager. This includes times
     * 	   in the pipeline where the user manually toggles the setting in the SystemManager or after each system
     * 	   update.
     *  </p>
     *
     * @param observer the function to be invoked when a component is attached
     * @return id of component observer added which can be later used to remove it
     */
    public int onComponentAttach(ComponentObserver<T> observer) {
        return this.attachComponentObservers.addObject(observer);
    }

    /**
     * Disconnects an attach observer from listening to component attaches.
     *
     * @param attachObserverID unique id assigned to componentObserver when it was registered through the
     * {@link #onComponentAttach(ComponentObserver) onComponentAttach} function.
     * @return instance of ComponentObserver disconnected or NULL if ID was invalid
     */
    public ComponentObserver<T> disconnectAttachObserver(int attachObserverID) {
        return this.attachComponentObservers.removeObject(attachObserverID);
    }

    void notifyAttachObservers(int entityID) {
        this.attachComponentObservers.forEach(observer -> observer.invoke(entityID, this));
    }

    /**
     * Assigns observer to be invoked whenever a component of this type is detached to an entity.
     * <p>
     *     Note: Observer won't be invoked immediately unless permitted by the SystemManager. This includes times
     * 	   in the pipeline where the user manually toggles the setting in the SystemManager or after each system
     * 	   update.
     * </p>
     *
     * @param observer the function to be invoked when a component is attached
     * @return id of component observer added which can be later used to remove it
     */
    public int onComponentDetach(ComponentObserver<T> observer) {
        return this.detachComponentObservers.addObject(observer);
    }

    /**
     * Disconnects a detach observer from listening to component detaches.
     *
     * @param detachObserverID unique id assigned to componentObserver when it was registered through the
     * {@link #onComponentDetach(ComponentObserver) onComponentDetach} function.
     * @return instance of ComponentObserver disconnected or NULL if ID was invalid
     */
    public ComponentObserver<T> disconnectDetachObserver(int detachObserverID) {
        return this.detachComponentObservers.removeObject(detachObserverID);
    }

    void notifyDetachObservers(int entityID) {
        this.detachComponentObservers.forEach(observer -> observer.invoke(entityID, this));
    }

    /**
     * Assigns observer to be invoked whenever a component of this type is changed.
     *
     * @param observer the function to be invoked when a component is changed
     * @return id of component observer added which can be later used to remove it
     */
    public int onComponentChange(ComponentObserver<T> observer) {
        return this.changeComponentObservers.addObject(observer);
    }

    /**
     * Disconnects a change observer from listening to component changes.
     *
     * @param changeObserverID unique id assigned to componentObserver when it was registered through the
     * {@link #onComponentChange(ComponentObserver) onComponentChange} function.
     * @return instance of ComponentObserver disconnected or NULL if ID was invalid
     */
    public ComponentObserver<T> disconnectChangeObserver(int changeObserverID) {
        return this.changeComponentObservers.removeObject(changeObserverID);
    }

    /**
     * User should call this function to notify system of changes to a component. All "onComponentChange" callbacks will
     * be triggered.
     *
     * @param entityID id of entity whose component data has changed
     */
    public void notifyChangeObservers(int entityID) {
        this.changeComponentObservers.forEach(observer -> observer.invoke(entityID, this));
    }

    /**
     * Creates a new ChangeBin instance for containing changed entities. Use sparingly and try to pass along the same
     * ChangeBin instance whenever possible.
     *
     * @return ChangeBin instance containing all entities who have changed state since last iteration
     */
    public Bin createChangeBin() {
        Bin changeBin = new Bin(this.changeComponentObservers.size(), this);
        this.onComponentChange((entityID, componentType) -> changeBin.addInternalEntity(entityID));
        return changeBin;
    }

    T attachInternalEntity(int entityID) {
        T componentInstance = this.componentConstructor.get();
        this.mappedComponentInstances.setObject(entityID, componentInstance);
        return componentInstance;
    }

    T detachInternalEntity(int entityID) {
        if (!this.mappedComponentInstances.contains(entityID)) return null;
        return this.mappedComponentInstances.removeObject(entityID);
    }

    public String toString() {
        return "ComponentType(component=" + this.componentClass.getSimpleName() + ")";
    }
}
