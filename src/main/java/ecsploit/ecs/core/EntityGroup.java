package ecsploit.ecs.core;

import ecsploit.utils.collections.SparseList;

abstract class EntityGroup {

	final SparseList entities = new SparseList();
	
	public int size() {
		return entities.size();
	}
	
	void addInternalEntity(int entityID) {
		this.entities.add(entityID);
	}
	
	void removeInternalEntity(int entityID) {
		this.entities.fastRemove(entityID);
	}
	
	public boolean contains(int entityID) {
		return this.entities.contains(entityID);
	}

	/**
	 * Iterate through every entity in the entityGroup and perform an action on it.
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
		for (int i = entitySize - 1; i >= 0; i--) {
			action.accept(this.entities.fastGet(i));
		}
	}
}
