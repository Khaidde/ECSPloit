package ecsploit.ecs.core;

import ecsploit.utils.collections.SparseList;

/**
 * Group of entities which is updated with components which have recently been changed
 */
public final class Bin {

    private int observerID;
    private final EntityStream inputEventStream;

    private final SparseList entities = new SparseList();

    Bin(EntityStream inputEventStream) {
        this.inputEventStream = inputEventStream;
    }

    void setObserverID(int observerID) {
        this.observerID = observerID;
    }

    boolean contains(int entityID) {
        return this.entities.contains(entityID);
    }

    void addInternalEntity(int entityID) {
        this.entities.add(entityID);
    }

    /**
     * Iterate through every unhandled entity event and clear after each full iteration
     *
     * @param action invoked per entity
     */
    public void forEachEntity(EntityAction action) {
        if (entities.isEmpty()) return;
        int entitySize = this.entities.size();
        for (int i = entitySize - 1; i >= 0; i--) {
            action.accept(this.entities.fastGet(i));
        }
        this.entities.clear();
    }

    /**
     * Disconnects the bin from the entity stream. The bin will no longer be updated in real time.
     */
    public void dispose() {
        this.inputEventStream.disconnectObserver(observerID);
    }
}
