package ecsploit.main.test;

import ecsploit.ecs.core.Manager;
import ecsploit.ecs.core.SystemGroup;

public class SystemECSTest {

    public static void main(String[] args) {

        Manager manager = new Manager();

        manager.system(SystemGroup.from("OrderedSystems",
                new TestGameLib.Sys1(),
                new TestGameLib.Sys2(),
                new TestGameLib.Sys3(),
                new TestGameLib.Sys4(),
                new TestGameLib.Sys5(),
                new TestGameLib.Sys6(),
                new TestGameLib.Sys7(),
                new TestGameLib.Sys8(),
                new TestGameLib.Sys9(),
                new TestGameLib.Sys10(),
                new TestGameLib.Sys11(),
                new TestGameLib.Sys12(),
                new TestGameLib.Sys13(),
                new TestGameLib.Sys14()
        ));
        manager.system(new TestGameLib.RenderSystem());
        manager.system(new TestGameLib.MovementSystem());

        System.out.println(manager.viewSystems());

        manager.update();
    }
}
