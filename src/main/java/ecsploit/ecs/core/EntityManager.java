package ecsploit.ecs.core;

import ecsploit.utils.collections.DenseList;
import ecsploit.utils.debug.ToStringBuilder;

public final class EntityManager {

	private final DenseList<Entity> entities = new DenseList<>();
	private int totalRecycled;
	private int lastRecycledIndex = -1;

	private final Manager manager;

	EntityManager(Manager manager) {
		this.manager = manager;
	}

	int getEntitiesSize() {
		return this.entities.size() - totalRecycled;
	}

	public static class EntityNotFoundException extends RuntimeException {
		public EntityNotFoundException(String message) {
			super(message);
		}
	}

	/**
	 * See {@link Manager#get(int) wrapper} for more details.
	 * @throws EntityManager.EntityNotFoundException when active entity is not found
	 */
	Entity getEntity(int entityID) {
		Entity entity = entities.get(entityID);
		if (entity == null || entities.get(entityID).getID() != entityID) {
			throw new EntityNotFoundException("Entity id=" + entityID + " is not currently active and can't be retrieved");
		}
		return entity;
	}

	/**
	 * Creates a new entity instance with unique integer id.
	 *
	 * @return instance of entity
	 */
	Entity createEntity() {
		Entity entity;
		if (totalRecycled == 0) {
			entity = new Entity();
			entity.setID(entities.size());
			entities.add(entity);
		} else {
			entity = entities.get(lastRecycledIndex);
			int entityID = entity.getID();
			entity.setID(lastRecycledIndex);
			lastRecycledIndex = entityID;
			totalRecycled--;
		}
		manager.getComponentManager().createComponentBitsInternal(entity.getID());
		entity.setManager(manager);
		return entity;
	}

	/**
	 * Deletes entity and dependencies to entity groups.
	 * <p>
	 *     Note: Entity reference will no longer be valid. Continued use of Entity reference can lead to undefined
	 *     behavior.
	 * </p>
	 *
	 * @param entity instance of entity to be destroyed
	 */
	void destroyEntity(Entity entity) {
		manager.getComponentManager().deleteComponentBitsInternal(entity);
		int entityID = entity.getID();
		entity.setID(this.lastRecycledIndex);
		this.lastRecycledIndex = entityID;
		this.totalRecycled++;
	}

	void forEach(EntityAction action) {
		for (int i = 0; i < this.entities.size(); i++) {
			if (this.entities.get(i).getID() == i) {
				action.accept(i);
			}
		}
	}

	public String toString() {
		int[] toStringEntities = new int[entities.size() - totalRecycled];
		int counter = 0;
		for (int i = 0; i < this.entities.size(); i++) {
			if (this.entities.get(i).getID() == i) {
				toStringEntities[counter++] = this.entities.get(i).getID();
			}
		}
		return ToStringBuilder.from(this)
				.withPrim("numPooledInstances", this.entities.size())
				.withPrim("activeSize", toStringEntities.length)
				.withIntArray("activeEntities", toStringEntities)
				.toString();
	}
}
