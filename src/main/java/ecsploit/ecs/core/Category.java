package ecsploit.ecs.core;

import ecsploit.utils.collections.SparseList;

import java.util.Arrays;
import java.util.function.IntPredicate;

/**
 * Group of entities with common properties
 */
public class Category {

    final SparseList entities = new SparseList();

    protected final EntityStream addStream = new EntityStream();
    protected final EntityStream removeStream = new EntityStream();
    protected final EntityStream changeStream = new EntityStream();

    public int size() {
        return this.entities.size();
    }

    public boolean has(int entityID) {
        return this.entities.contains(entityID);
    }

    void addInternalEntity(int entityID) {
        this.entities.add(entityID);
        this.addStream.notifyObservers(entityID);
    }

    void removeInternalEntity(int entityID) {
        this.entities.fastRemove(entityID);
        this.removeStream.notifyObservers(entityID);
    }

    void notifyChangeObservers(int entityID) {
        this.changeStream.notifyObservers(entityID);
    }

    /**
     * Iterate through every entity in the category and perform an action on it.
     * <p>
     *     Note: iterate backwards to avoid potential conflicts with removing and adding components to entities during
     *     iteration. The logic behind it is magic, trust me :P
     * </p>
     *
     * @param action invoked per entity
     */
    public void forEachEntity(EntityAction action) {
        for (int i = 0; i < this.entities.size(); i++) {
            action.accept(this.entities.fastGet(i));
        }
    }

    /**
     * @return list of all entityIDs associated with the category
     */
    public int[] getEntityIDs() {
        return Arrays.copyOf(this.entities.getInnerList(), this.size());
    }

    /**
     * Generate a Category from all entities in the current category where all
     *
     * @param filterCondition condition under which all the entities in the new Category fulfill
     * @return a Category where all entities fulfill the filtered condition
     */
    public Category filter(IntPredicate filterCondition) {
        Category filteredCat = new Category();
        if (!entities.isEmpty()) {
            for (int i = this.entities.size() - 1; i >= 0; i--) {
                int eID = this.entities.fastGet(i);
                if (filterCondition.test(eID)) {
                    filteredCat.addInternalEntity(eID);
                }
            }
        }
        this.addStream.connectObserver(eID -> {
            if (filterCondition.test(eID)) filteredCat.addInternalEntity(eID);
        });
        this.removeStream.connectObserver(eID -> {
            if (filteredCat.has(eID)) filteredCat.removeInternalEntity(eID);
        });
        this.changeStream.connectObserver(eID -> {
            if (!filteredCat.has(eID) && filterCondition.test(eID)) {
                filteredCat.addInternalEntity(eID);
            } else if (filteredCat.has(eID)){
                filteredCat.removeInternalEntity(eID);
            }
        });
        return filteredCat;
    }
}
