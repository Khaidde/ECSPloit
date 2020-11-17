package ecsploit.main.test;

import ecsploit.ecs.core.*;
import ecsploit.main.test.TestGameLib.*;
import ecsploit.utils.debug.Console;
import ecsploit.utils.debug.SimpleProfiler;

public class ComponentECSTest {

    private static final Console CONSOLE = Console.getConsole(ComponentECSTest.class);

    private static final int N = 100_000;

    private final Manager manager;

    private final ComponentType<Transform> transformComponentType;
    private final ComponentType<Sprite> spriteComponentType;

    public ComponentECSTest() {
        this.manager = new Manager();

        manager.system(manager.systemGroupFrom("OrderedSystems",
                new Sys1(),
                new Sys2(),
                new Sys3(),
                new Sys4(),
                new Sys5(),
                new Sys6(),
                new Sys7(),
                new Sys8(),
                new Sys9(),
                new Sys10(),
                new Sys11(),
                new Sys12(),
                new Sys13(),
                new Sys14()
        ));
        manager.system(new RenderSystem());
        manager.system((new MovementSystem()));

        System.out.println(manager.viewSystems());

        this.transformComponentType = manager.type(Transform.class);
        this.spriteComponentType = manager.type(Sprite.class);

        for(int n = 0; n < 50; n++) {
            this.attachComponentType();
            this.destroyEntities();
            this.attachComponentClass();
            this.destroyEntities();
        }

        SimpleProfiler profiler = new SimpleProfiler();

        profiler.start();
        this.attachComponentType();
        CONSOLE.info("ComponentTypeTest1: " + profiler.stop() + " ms");
        this.destroyEntities();

        profiler.start();
        this.attachComponentClass();
        CONSOLE.info("ClassTest1: " + profiler.stop() + " ms");
        this.destroyEntities();

        profiler.start();
        this.attachComponentType();
        CONSOLE.info("ComponentTypeTest2: " + profiler.stop() + " ms");
        this.destroyEntities();

        profiler.start();
        this.attachComponentClass();
        CONSOLE.info("ClassTest2: " + profiler.stop() + " ms");
        this.destroyEntities();

        for (int i = 0; i < 3; i ++) {
            Entity entity = manager.entity();
            entity.attach(Transform.class);
            entity.attach(Sprite.class);
        }
        manager.update();
    }

    public void destroyEntities() {
        for (int i = 0; i < N; i++) {
            Entity entity = manager.get(i);
            manager.destroy(entity);
        }
    }

    public void attachComponentClass() {
        for (int i = 0; i < N; i++) {
            Entity entity = manager.entity();
            entity.attach(Transform.class).setPos(-12, 34);
            entity.attach(Sprite.class).setImagePath("NewTexture.png");
        }
    }

    public void attachComponentType() {
        for (int i = 0; i < N; i++) {
            Entity entity = manager.entity();
            entity.attachT(transformComponentType).setPos(-12, 34);
            entity.attachT(spriteComponentType).setImagePath("NewTexture.png");
        }
    }

    public static void main(String[] args) {
        new ComponentECSTest();
    }
}
