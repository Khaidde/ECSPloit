package ecsploit.main.test;

import java.text.DecimalFormat;
import java.util.Random;

import ecsploit.ecs.core.*;
import ecsploit.main.test.TestGameLib.*;
import ecsploit.utils.debug.Console;
import ecsploit.utils.debug.SimpleProfiler;

public class FunctionalECSTest {

	private static final Console CONSOLE = Console.getConsole(FunctionalECSTest.class);
    private static final SimpleProfiler profiler = new SimpleProfiler();

	private static final double PERCENT_ATTACH_COMPONENT = 0.3;
	private static final double PERCENT_DETACH_COMPONENT = 0.5;
	private static final long N_ENTITIES = 50_000;
	private static final long N_UPDATES = 400;

	private final Manager manager;
    private final Random random;
    private final DecimalFormat decFormat;
    private boolean showOutput = false;

    private final ComponentType<Transform> transformType;
    private final ComponentType<Velocity> velocityType;
    private final ComponentType<Sprite> spriteType;
    private final ComponentType<TransformContainer> transformContainerType;

    private final MovementSystem movementSystem;

    public FunctionalECSTest() {
		this.random = new Random();

		this.manager = new Manager();

		//System creation, registration and injection
		this.movementSystem = new MovementSystem();

		profiler.start();
		manager.system(movementSystem);
		CONSOLE.info("System Injection: " + profiler.stop() + " ms");

		this.decFormat = new DecimalFormat("#,###");
		CONSOLE.info("N_ENTITIES: " + decFormat.format(N_ENTITIES));
		CONSOLE.info("N_UPDATES: " + decFormat.format(N_UPDATES));
		CONSOLE.info("N_ITERATIONS: " + decFormat.format(N_ENTITIES * N_UPDATES));
		CONSOLE.info("---------------------------------");

		this.transformType = manager.type(Transform.class);
		this.transformType.registerConstructor(Transform::new);

		this.velocityType = manager.type(Velocity.class);
		this.velocityType.registerConstructor(Velocity::new);

		this.spriteType = manager.type(Sprite.class);
		this.spriteType.registerConstructor(Sprite::new);

		this.transformContainerType = manager.type(TransformContainer.class);
		this.transformContainerType.registerConstructor(TransformContainer::new);

		for (int i = 0; i < 20; i++) {
			if (i == 19) this.showOutput = true;
			this.doCreationTest();
			this.doAttachTest();

			this.doBenchmark1();
			this.doBenchmark2();

			this.doDetachTest();
			this.doBenchmark3();

			this.doAttachTest();
			this.doBenchmark4();

			this.doDestructionTest();
		}
	}

	void doCreationTest() {
		//Player Creation
		profiler.start();
		for (int i = 0; i < N_ENTITIES; i++) {
			manager.entity();
		}
		if (showOutput) CONSOLE.info("EntityCreationTest: (totalEntities=" + this.manager.sizeOfEntities() + ") " + profiler.stop() + " ms");
	}

	void doDestructionTest() {
		//Player Destruction
		profiler.start();
		for (int i = 0; i < N_ENTITIES; i++) {
			Entity entity = manager.get(i);
			if (entity != null) {
				manager.destroy(entity);
			}
		}
		if (showOutput) CONSOLE.info("EntityDestructionTest: (totalEntities=" + this.manager.sizeOfEntities() + ") " + profiler.stop() + " ms");
	}

	void doAttachTest() {
		//Attach Test
		profiler.start();
		Entity player;
		for (int i = 0; i < N_ENTITIES; i++) {
			player = manager.get(i);
			if (random.nextDouble() < PERCENT_ATTACH_COMPONENT) {
				manager.attachT(player, transformType).setPos(-10, 10);
			}
			if (random.nextDouble() < PERCENT_ATTACH_COMPONENT) {
				manager.attachT(player, velocityType).setVelocity(-random.nextInt(20), -19);
			}
			if (random.nextDouble() < PERCENT_ATTACH_COMPONENT) {
				manager.attachT(player, spriteType).setImagePath("NewTexture.png");
			}
			if (random.nextDouble() < PERCENT_ATTACH_COMPONENT) {
				manager.attachT(player, transformContainerType);
			}
		}
		if (showOutput) CONSOLE.info("ComponentAttachTest: " + profiler.stop() + " ms");
	}

	void doDetachTest() {
		//Random Detach Test
		profiler.start();
		Entity detachPlayer;
		for (int i = 0; i < N_ENTITIES; i++) {
			detachPlayer = manager.get(i);
			if (random.nextDouble() < PERCENT_DETACH_COMPONENT) manager.detachT(detachPlayer, transformType);
			if (random.nextDouble() < PERCENT_DETACH_COMPONENT) manager.detachT(detachPlayer, velocityType);
			manager.detachT(detachPlayer, spriteType);
			manager.detachT(detachPlayer, transformContainerType);
		}
		if (showOutput) CONSOLE.info("RandomDetachTest: " + profiler.stop() + " ms");
	}

	void doBenchmark1() {
		//Benchmark 1: Naive Approach
		profiler.start();
		int iterations1 = 0;
		Transform transform;
		Velocity velocity;
		Entity entity;
		for (int i = 0; i < N_UPDATES; i++) {
			for (int j = 0; j < N_ENTITIES; j++) {
				entity = manager.get(j);
				if (entity.has(transformType) && entity.has(velocityType) && entity.has(spriteType)) {
					transform = entity.get(transformType);
					velocity = entity.get(velocityType);

					transform.setPos(transform.getX() + velocity.getVx(), transform.getY() + velocity.getVy());
					iterations1++;
				}
			}
		}
		if (showOutput) CONSOLE.info("Benchmark 1: (iterations=" + decFormat.format(iterations1) + ") "  + profiler.stop() + " ms");
	}

	void doBenchmark2() {
		//Benchmark 2: System with entityGroup approach
		profiler.start();
		for (int i = 0; i < N_UPDATES; i++) {
			manager.update();
		}
		if (showOutput) CONSOLE.info("Benchmark 2: (iterations=" + decFormat.format(N_UPDATES * movementSystem.physicsGroup.size()) + ") " + profiler.stop() + " ms");
	}

	void doBenchmark3() {
		//Benchmark 3: After large batch detach
		profiler.start();
		for (int i = 0; i < N_UPDATES; i++) {
			manager.update();
		}
		if (showOutput) CONSOLE.info("Benchmark 3: (iterations=" + decFormat.format(N_UPDATES * movementSystem.physicsGroup.size()) + ") " + profiler.stop() + " ms");
	}

	void doBenchmark4() {
		//Benchmark 4: After large batch attach
		profiler.start();
		for (int i = 0; i < N_UPDATES; i++) {
			manager.update();
		}
		if (showOutput) CONSOLE.info("Benchmark 4: (iterations=" + decFormat.format(N_UPDATES * movementSystem.physicsGroup.size()) + ") " + profiler.stop() + " ms");
	}
	
	public static void main(String[] args) {
		new FunctionalECSTest();
	}
}
