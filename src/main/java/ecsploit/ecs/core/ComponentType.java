package ecsploit.ecs.core;

import ecsploit.ecs.event.ComponentObserver;
import ecsploit.utils.collections.DenseList;
import ecsploit.utils.collections.PackedObjectList;

import java.util.function.Supplier;

public class ComponentType<T extends Component> {

    private final Class<T> componentClass;
    private final int id;

    private Supplier<T> componentConstructor;

    private final PackedObjectList<T> mappedComponentInstances = new PackedObjectList<>();

    private final DenseList<ComponentObserver> attachComponentObservers = new DenseList<>();
    private final DenseList<ComponentObserver> detachComponentObservers = new DenseList<>();

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
     */
    public void onComponentAttach(ComponentObserver observer) {
        this.attachComponentObservers.add(observer);
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
     */
    public void onComponentDetach(ComponentObserver observer) {
        this.detachComponentObservers.add(observer);
    }

    T attachInternalComponent(int entityID) {
        T componentInstance = this.componentConstructor.get();
        this.mappedComponentInstances.addOrReplaceObject(entityID, componentInstance);
        return componentInstance;
    }

    void notifyAttachObservers(int entityID) {
        this.attachComponentObservers.forEach(observer -> observer.invoke(entityID, this));
    }

    T detachInternalComponent(int entityID) {
        if (!this.mappedComponentInstances.contains(entityID)) return null;
        return this.mappedComponentInstances.removeObject(entityID);
    }

    void notifyDetachObservers(int entityID) {
        this.detachComponentObservers.forEach(observer -> observer.invoke(entityID, this));
    }

    public String toString() {
        return "ComponentType(component=" + this.componentClass.getSimpleName() + ")";
    }
}
