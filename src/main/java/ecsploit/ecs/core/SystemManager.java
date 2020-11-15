package ecsploit.ecs.core;

import ecsploit.ecs.injection.CatTarget;
import ecsploit.ecs.injection.TypeTarget;
import ecsploit.utils.debug.ToStringBuilder;

import java.lang.reflect.Field;

public final class SystemManager {

	private static void injectSystemFields(Manager manager, BaseSystem system) {
		Class<? extends BaseSystem> systemClass = system.getClass();
		for (Field field : systemClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(TypeTarget.class)) {
				TypeTarget componentTypeAnnotation = field.getAnnotation(TypeTarget.class);
				Class<? extends Component> componentClass = componentTypeAnnotation.value();

				field.setAccessible(true);
				try {
					field.set(system, manager.getComponentManager().getComponentType(componentClass));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				continue;
			}
			if (field.isAnnotationPresent(CatTarget.class)) {
				CatTarget componentTypeAnnotation = field.getAnnotation(CatTarget.class);
				Class<? extends Component>[] componentClasses = componentTypeAnnotation.value();
				Category category = manager.getComponentManager().getCategory(componentClasses);

				field.setAccessible(true);
				try {
					field.set(system, category);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private final SystemGroup rootSystemGroup;
	
	private final Manager manager;
	
	SystemManager(Manager manager) {
		this.manager = manager;

		this.rootSystemGroup = SystemGroup.from("Root");
	}

	void register(BaseSystem system) {
		SystemManager.injectSystemFields(manager, system);

		if (ExecuteSystem.class.isAssignableFrom(system.getClass())) {
			this.rootSystemGroup.insert((ExecuteSystem) system);
		}

		system.init(manager);
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
