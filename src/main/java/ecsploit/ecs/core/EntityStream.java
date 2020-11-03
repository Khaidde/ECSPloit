package ecsploit.ecs.core;

import ecsploit.utils.collections.PackedObjectList;

/**
 * Stream of entity changes which notifies all observers when a change occurs
 */
public final class EntityStream {

    private final PackedObjectList<EntityObserver> entityObservers = new PackedObjectList<>();

    EntityStream() {}

    int connectObserver(EntityObserver observer) {
        return this.entityObservers.addObject(observer);
    }

    EntityObserver disconnectObserver(int observerID) {
        return this.entityObservers.removeObject(observerID);
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
