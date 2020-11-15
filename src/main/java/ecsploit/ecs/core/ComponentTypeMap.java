package ecsploit.ecs.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ComponentTypeMap {

	private final Map<Class<? extends Component>, ComponentType<?>> componentTypes = new HashMap<>();
	private int idCounter = 0;

	private <T extends Component> Supplier<T> getDefaultConstructor(Class<T> componentClass) {
		try {
			final Constructor<T> componentConstructor = componentClass.getDeclaredConstructor();
			return () -> {
				try {
					return componentConstructor.newInstance();
				} catch (IllegalAccessException e) {
					throw new IllegalAccessError(componentClass.getSimpleName() + ".class is not a public class or " +
							"has no public constructor. Component may also be inaccessible due to module protection");
				} catch (InstantiationException | InvocationTargetException e) {
					e.printStackTrace();
				}
				return null;
			};
		} catch (NoSuchMethodException e) {
			throw new NoSuchMethodError(componentClass.getSimpleName() + ".class needs to have an empty constructor");
		}
	}

	@SuppressWarnings("unchecked")
	<T extends Component> ComponentType<T>[] getComponentTypes() {
		return (ComponentType<T>[]) componentTypes.values().toArray(new ComponentType<?>[0]);
	}

	@SuppressWarnings("unchecked")
	<T extends Component> ComponentType<T> getFromID(int componentID) {
		if (componentID >= componentTypes.size()) return null;
		for (ComponentType<?> componentType: componentTypes.values()) {
			if (componentType.getID() == componentID) return (ComponentType<T>) componentType;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	 <T extends Component> ComponentType<T> getComponentType(Class<T> componentClass) {
		return (ComponentType<T>) componentTypes.computeIfAbsent(componentClass, key -> {
			ComponentType<T> componentType = new ComponentType<>(componentClass, idCounter++);
			componentType.registerConstructor(this.getDefaultConstructor(componentClass));
			return componentType;
		});
	}
	
}
