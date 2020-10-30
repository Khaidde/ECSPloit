package ecsploit.ecs.core;

import ecsploit.utils.debug.ToStringBuilder;

import java.util.Arrays;

public final class EntityManager {

	private Entity[] entityPool = new Entity[1000];
	private int[] entities = new int[64];
	private int maxCapacity = 0;

	private int totalRecycled;
	private int lastRecycledIndex = -1;

	private final Manager manager;

	EntityManager(Manager manager) {
		this.manager = manager;
	}

	int getEntitiesSize() {
		return maxCapacity - totalRecycled;
	}

	public static class EntityNotFoundException extends RuntimeException {
		public EntityNotFoundException(String message) {
			super(message);
		}
	}

	/**
	 * See {@link Manager#get(int) wrapper} for more details.
	 * @throws EntityNotFoundException when active entity is not found
	 */
	Entity getEntity(int entityID) {
		if (entityID >= maxCapacity || entities[entityID] != entityID) {
			throw new EntityNotFoundException("Entity id=" + entityID + " is not currently active and can't be retrieved");
		}
		if (entityID >= entityPool.length) {
			this.entityPool = Arrays.copyOf(this.entityPool, entityID + (entityID >>> 1) + 1);
		}
		if (this.entityPool[entityID] == null) {
			this.entityPool[entityID] = new Entity(entityID, manager);
		}
		return this.entityPool[entityID];
	}

	/**
	 * Creates unique integer id representation a new entity.
	 *
	 * @return id of new entity
	 */
	int createEntityID() {
		int entityID;
		if (totalRecycled == 0) {
			if (maxCapacity >= this.entities.length) {
				this.entities = Arrays.copyOf(this.entities, maxCapacity + (maxCapacity >>> 1) + 1);
			}
			entityID = maxCapacity;
			entities[maxCapacity] = maxCapacity;
			maxCapacity++;
		} else {
			int lastEntityID = entities[lastRecycledIndex];
			entityID = entities[lastRecycledIndex] = lastRecycledIndex;
			lastRecycledIndex = lastEntityID;
			totalRecycled--;
		}
		manager.getComponentManager().createComponentBitsInternal(entityID);
		return entityID;
	}

	/**
	 * Deletes entity and dependencies to entity groups.
	 * <p>
	 *     Note: Entity reference will no longer be valid. Continued use of Entity reference can lead to undefined
	 *     behavior.
	 * </p>
	 *
	 * @param entityID id of entity to be destroyed
	 */
	void destroyEntity(int entityID) {
		if (entityID >= maxCapacity || entities[entityID] != entityID) {
			throw new EntityNotFoundException("Entity id=" + entityID + " is not currently active and can't be destroyed");
		}
		manager.getComponentManager().deleteComponentBitsInternal(entityID);
		this.entities[entityID] = this.lastRecycledIndex;
		this.lastRecycledIndex = entityID;
		this.totalRecycled++;
	}

	void forEach(EntityAction action) {
		for (int i = 0; i < this.maxCapacity; i++) {
			if (this.entities[i] == i) {
				action.accept(i);
			}
		}
	}

	public String toString() {
		int[] toStringEntities = new int[this.getEntitiesSize()];
		int counter = 0;
		for (int i = 0; i < this.maxCapacity; i++) {
			if (this.entities[i] == i) {
				toStringEntities[counter++] = this.entities[i];
			}
		}
		return ToStringBuilder.from(this)
				.withPrim("maxCapacity", this.maxCapacity)
				.withPrim("activeSize", toStringEntities.length)
				.withIntArray("activeEntities", toStringEntities)
				.toString();
	}
}
