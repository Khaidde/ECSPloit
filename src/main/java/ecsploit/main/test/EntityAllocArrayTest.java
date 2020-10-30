package ecsploit.main.test;

import ecsploit.ecs.core.Entity;
import ecsploit.ecs.core.Manager;

public class EntityAllocArrayTest {

    public static void main(String[] args) {

        Manager manager = new Manager();

        for (int i = 0; i < 10; i++) {
            manager.entity();
        }
        System.out.println(manager.viewEntities());

        for (int i = 0; i < 10; i += 3) {
            if (i < 5) {
                manager.destroy(manager.get(i));
            } else {
                manager.destroyID(i);
            }
        }
        System.out.println(manager.viewEntities());

        for (int i = 0; i < 10; i++) {
            manager.entity();
            if (i < 5) System.out.println(manager.viewEntities());
        }
        System.out.println(manager.viewEntities());

        //Show that an entity instance exists for every valid entityID
        for (int i = 0; i < manager.sizeOfEntities(); i++) {
            manager.get(i);
        }

        Entity a = manager.get(4);
        Entity b = manager.get(4);
        System.out.println(a.hashCode() + "::" + b.hashCode());
    }
}
