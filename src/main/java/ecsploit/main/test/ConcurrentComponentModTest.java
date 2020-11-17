package ecsploit.main.test;

import ecsploit.ecs.core.Category;
import ecsploit.ecs.core.ComponentType;
import ecsploit.ecs.core.ExecuteSystem;
import ecsploit.ecs.core.Manager;
import ecsploit.ecs.injection.CatTarget;
import ecsploit.ecs.injection.TypeTarget;
import ecsploit.main.test.TestGameLib.*;

public class ConcurrentComponentModTest {

    public static void main(String[] args) {

        Manager manager = new Manager();

        for (int i = 0; i < 7; i++) {
            int entityID = manager.entityID();
            manager.attach(entityID, Transform.class).setPos(0, i);
        }

        for (int i = 0; i < 7; i++) manager.entityID();

        manager.system(new TestSystem());
        manager.update();
        manager.update();
    }

    static class TestSystem extends ExecuteSystem {

        @CatTarget(Transform.class)
        Category transformCat;

        @TypeTarget(Transform.class)
        public ComponentType<Transform> transformType;

        private Manager manager;

        public void init(Manager manager) {
            this.manager = manager;
        }

        protected void execute() {
            System.out.println("=>  Initial Size with ForEach: " + transformCat.size());
            transformCat.forEachEntity(entityID -> {
                Transform transform = transformType.retrieve(entityID);

                if (transform.getY() == 6) {
                    manager.detachT(entityID, transformType);
                    assert transformCat.has(entityID);
                    System.out.println("TransformComponent with y=6 has been detached");
                }
                if (transform.getY() == 4) {
                    manager.detachT(entityID, transformType);
                    assert transformCat.has(entityID);
                    System.out.println("TransformComponent with y=4 has been detached");
                }
                System.out.println("Size at y = " + transform.getY() + ": " + transformCat.size());
            });

            manager.cleanComponents();

            System.out.println("=>  Initial Size with entityID array: " + transformCat.size());
            for (int entityID: transformCat.getEntityIDs()) {
                Transform transform = transformType.retrieve(entityID);

                if (transform.getY() == 3) {
                    manager.attachT(manager.entityID(), transformType).setPos(-10, -10);
                    System.out.println("New TransformComponent has been attached");
                }
                System.out.println("Size at y = " + transform.getY() + ": " + transformCat.size());
            }
        }
    }
}
