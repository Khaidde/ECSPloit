package ecsploit.ecs.core;

import ecsploit.ecs.injection.CatTarget;
import ecsploit.ecs.injection.TypeTarget;
import ecsploit.utils.debug.ToStringBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;

public final class SystemManager {

	private static void injectSystemFields(Manager manager, BaseSystem system) {
		Class<? extends BaseSystem> systemClass = system.getClass();
		for (Field field : systemClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(TypeTarget.class)) {
				TypeTarget componentTypeAnnotation = field.getAnnotation(TypeTarget.class);
				Class<? extends Component> componentClass = componentTypeAnnotation.value();

				try {
					field.setAccessible(true);
					field.set(system, manager.getComponentManager().getComponentType(componentClass));
				} catch (InaccessibleObjectException | IllegalArgumentException | IllegalAccessException e) {
					throw new IllegalAccessError("Field to be injected with TypeTarget=" +
							componentClass.getSimpleName() + ".class is not public or inaccessible due to module " +
							"protection in " + system.getClass().getSimpleName() + ".class");
				}
				continue;
			}
			if (field.isAnnotationPresent(CatTarget.class)) {
				CatTarget componentTypeAnnotation = field.getAnnotation(CatTarget.class);
				Class<? extends Component>[] componentClasses = componentTypeAnnotation.value();
				Category category = manager.getComponentManager().getCategory(componentClasses);

				try {
					field.setAccessible(true);
					field.set(system, category);
				} catch (InaccessibleObjectException | IllegalArgumentException | IllegalAccessException e) {
					StringBuilder componentTypes = new StringBuilder();
					for (int i = 0; i < componentClasses.length - 1; i++) {
						componentTypes.append(componentClasses[i].getSimpleName()).append(", ");
					}
					componentTypes.append(componentClasses[componentClasses.length - 1].getSimpleName());
					throw new IllegalAccessError("Field to be injected with Category=[" +
							componentTypes + "] is not public or inaccessible due to module " +
							"protection in " + system.getClass().getSimpleName() + ".class");
				}
			}
		}
	}

	private final SystemGroup rootSystemGroup;
	
	private final Manager manager;
	
	SystemManager(Manager manager) {
		this.manager = manager;

		this.rootSystemGroup = this.createSystemGroup("Root");
	}

	void register(BaseSystem system) {
		SystemManager.injectSystemFields(manager, system);

		if (ExecuteSystem.class.isAssignableFrom(system.getClass())) {
			this.rootSystemGroup.insert((ExecuteSystem) system);
		}

		system.init(manager);
	}

	SystemGroup createSystemGroup(String name, ExecuteSystem... systems) {
		SystemGroup systemGroup = new SystemGroup(name, manager);
		for (ExecuteSystem system: systems) {
			systemGroup.insert(system);
		}
		return systemGroup;
	}
	
	void update() {
		this.rootSystemGroup.execute();
	}

	public String toString() {
		return ToStringBuilder.from(this)
				.withObj("Root", rootSystemGroup)
				.toString();
	}

}
