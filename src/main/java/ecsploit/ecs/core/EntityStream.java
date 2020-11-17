package ecsploit.ecs.core;

import ecsploit.utils.collections.DenseList;
import ecsploit.utils.collections.SparseList;

/**
 * Stream of entity changes which notifies all observers when a change occurs
 */
public final class EntityStream {

    private final DenseList<EntityObserver> entityObservers = new DenseList<>();
    private final SparseList obseverIDs = new SparseList();

    EntityStream() {}

    int connectObserver(EntityObserver observer) {
        int id = this.obseverIDs.size();
        this.obseverIDs.add(id);
        this.entityObservers.add(observer);
        return id;
    }

    EntityObserver disconnectObserver(int observerID) {
        int index = this.obseverIDs.indexOf(observerID);
        if (index == -1) return null;
        EntityObserver object = entityObservers.fastRemove(index);
        this.obseverIDs.fastRemove(observerID);
        return object;
    }

    void notifyObservers(int entityID) {
        this.entityObservers.forEach(observer -> observer.update(entityID));
    }

    /**
     * Creates a new Bin instance for containing changed entities. Use sparingly and try to pass along the same
     * Bin instance whenever possible.
     *
     * @return Bin instance containing all entities who have changed state since last iteration
     */
    public Bin createBin() {
        Bin changeBin = new Bin(this);
        int observerID = this.connectObserver(eID -> {
            if (!changeBin.contains(eID)) changeBin.addInternalEntity(eID);
        });
        changeBin.setObserverID(observerID);
        return changeBin;
    }

}
