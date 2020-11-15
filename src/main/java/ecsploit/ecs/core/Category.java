package ecsploit.ecs.core;

import ecsploit.utils.collections.SparseList;

import java.util.function.IntPredicate;

/**
 * Group of entities with common properties
 */
public final class Category {

    private final SparseList entities = new SparseList();

    protected final EntityStream addStream = new EntityStream();
    protected final EntityStream removeStream = new EntityStream();
    protected final EntityStream changeStream = new EntityStream();

    private final ComponentManager componentManager;

    Category(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    public int size() {
        return this.entities.size();
    }

    boolean contains(int entityID) {
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
        if (entities.isEmpty()) return;
        int entitySize = this.entities.size();
        componentManager.setToDeferredStrategy();
        for (int i = entitySize - 1; i >= 0; i--) {
            action.accept(this.entities.fastGet(i));
        }
        componentManager.clean();
        componentManager.setToImmediateStrategy();
    }

    /**
     * Generate a Category from all entities in the current category where all
     *
     * @param filterCondition condition under which all the entities in the new Category fulfill
     * @return a Category where all entities fulfill the filtered condition
     */
    public Category filter(IntPredicate filterCondition) {
        Category filteredCat = new Category(componentManager);
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
            if (filteredCat.contains(eID)) filteredCat.removeInternalEntity(eID);
        });
        this.changeStream.connectObserver(eID -> {
            if (!filteredCat.contains(eID) && filterCondition.test(eID)) {
                filteredCat.addInternalEntity(eID);
            } else if (filteredCat.contains(eID)){
                filteredCat.removeInternalEntity(eID);
            }
        });
        return filteredCat;
    }
}
