package ecsploit.main.test;

import ecsploit.ecs.core.Manager;

public class EntityAllocArrayTest {

    public static void main(String[] args) {

        Manager manager = new Manager();

        for (int i = 0; i < 10; i++) {
            manager.entity();
        }
        System.out.println(manager.viewEntities());

        for (int i = 0; i < 10; i += 3) {
            manager.destroy(manager.get(i));
        }
        System.out.println(manager.viewEntities());

        for (int i = 0; i < 11; i++) {
            manager.entity();
        }
        System.out.println(manager.viewEntities());
    }
}
