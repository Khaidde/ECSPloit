package ecsploit.ecs.core;

/**
 * Group of entities which is updated with components which have recently been changed
 */
public class Bin extends EntityGroup {

    private final int observerID;
    private final ComponentType<? extends Component> componentType;

    Bin(int observerID, ComponentType<? extends Component> componentType) {
        this.observerID = observerID;
        this.componentType = componentType;
    }

    /**
     * Iterate through every unhandled entity event and clear after each full iteration
     *
     * @param action invoked per entity
     */
    public void forEachEntity(EntityAction action) {
        super.forEachEntity(action);
        this.entities.clear();
    }

    /**
     * Disconnects the bin from the affiliated group. The bin will no longer be updated in real time.
     */
    public void dispose() {
        this.componentType.disconnectChangeObserver(observerID);
    }
}
