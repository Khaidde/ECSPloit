package ecsploit.main.test;

import ecsploit.ecs.core.*;
import ecsploit.ecs.injection.TypeTarget;
import ecsploit.ecs.injection.CatTarget;
import ecsploit.ecs.injection.ExecuteAfter;
import ecsploit.ecs.injection.ExecuteBefore;
import ecsploit.utils.debug.ToStringBuilder;

class TestGameLib {

    private TestGameLib() {}

    public static final class Transform extends Component {
        private int x;
        private int y;

        public Transform setPos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public String toString() {
            return ToStringBuilder.from(this)
                    .withPrim("x", x)
                    .withPrim("y", y)
                    .toString();
        }
    }

    public static final class Velocity extends Component {
        private int vx;
        private int vy;

        public Velocity setVelocity(int vx, int vy) {
            this.vx = vx;
            this.vy = vy;
            return this;
        }

        public int getVx() {
            return vx;
        }

        public int getVy() {
            return vy;
        }

        public String toString() {
            return ToStringBuilder.from(this)
                    .withPrim("vx", vx)
                    .withPrim("vy", vy)
                    .toString();
        }
    }

    public static final class Sprite extends Component {

        private String imagePath;

        public Sprite setImagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public String getImagePath() {
            return imagePath;
        }

        public String toString() {
            return ToStringBuilder.from(this)
                    .withPrim("imgPath", imagePath)
                    .toString();
        }
    }

    public static final class TransformContainer extends Component {

        private Transform transform;

        public String toString() {
            return ToStringBuilder.from(this)
                    .withObj("transform", transform)
                    .toString();
        }

    }

    public static final class MovementSystem extends ExecuteSystem {

        @TypeTarget(Transform.class)
        protected ComponentType<Transform> transformType;
        @TypeTarget(Velocity.class)
        protected ComponentType<Velocity> velocityType;

        @CatTarget({Transform.class, Velocity.class, Sprite.class})
        protected Category physicsCat;

        public void execute() {
            physicsCat.forEachEntity(eID -> {
                Transform transform = transformType.retrieve(eID);
                Velocity velocity = velocityType.retrieve(eID);

                transform.x += velocity.vx;
                transform.y += velocity.vy;
            });
        }
    }

    public static final class RenderSystem extends ExecuteSystem {

        @TypeTarget(Transform.class) private ComponentType<Transform> transformType;
        @TypeTarget(Sprite.class) private ComponentType<Sprite> spriteType;

        @CatTarget({Transform.class, Sprite.class}) private Category renderableEntitiesCat;

        protected void execute() {
            renderableEntitiesCat.forEachEntity(eID -> {
                Transform transform = transformType.retrieve(eID);
                Sprite sprite = spriteType.retrieve(eID);

                System.out.println("Sprite at " + sprite.getImagePath() + " with coordinates x=" + transform.getX() + " and y=" + transform.getY());
            });
        }
    }

    @ExecuteBefore(Sys2.class)
    public static final class Sys1 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys1");
        }
    }

    public static final class Sys2 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys2");
        }
    }

    @ExecuteAfter(Sys1.class)
    public static final class Sys3 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys3");
        }
    }

    @ExecuteBefore(Sys3.class)
    public static final class Sys4 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys4");
        }
    }

    @ExecuteAfter(Sys3.class)
    public static final class Sys5 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys5");
        }
    }

    @ExecuteAfter(Sys5.class)
    public static final class Sys6 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys6");
        }
    }

    @ExecuteBefore(Sys10.class)
    @ExecuteAfter({Sys6.class, Sys4.class})
    public static final class Sys7 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys7");
        }
    }

    @ExecuteAfter(Sys2.class)
    @ExecuteBefore(Sys7.class)
    public static final class Sys8 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys8");
        }
    }

    @ExecuteAfter({Sys2.class, Sys7.class})
    public static final class Sys9 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys9");
        }
    }

    public static final class Sys10 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys10");
        }
    }

    @ExecuteBefore(Sys8.class)
    @ExecuteAfter(Sys5.class)
    public static final class Sys11 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys11");
        }
    }

    @ExecuteAfter(Sys3.class)
    @ExecuteBefore(Sys13.class)
    public static final class Sys12 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys12");
        }
    }

    @ExecuteBefore(Sys14.class)
    public static final class Sys13 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys13");
        }
    }

    @ExecuteBefore(Sys2.class)
    public static final class Sys14 extends ExecuteSystem {
        protected void execute() {
            System.out.println("Sys14");
        }
    }

}
