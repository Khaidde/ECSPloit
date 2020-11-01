package ecsploit.ecs.core;

import ecsploit.ecs.injection.TypeTarget;
import ecsploit.ecs.injection.CatTarget;
import ecsploit.utils.debug.ToStringBuilder;

import java.lang.reflect.Field;

public final class SystemGroup extends AbstractSystem {

    private static void injectSystemFields(Manager manager, AbstractSystem system) {
        system.init(manager);

        Class<? extends AbstractSystem> systemClass = system.getClass();
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

    private final String name;
    private AbstractSystem[] systems = new AbstractSystem[]{};
    private final SystemGraph systemGraph = new SystemGraph();

    private boolean iterating = false;

    private Manager manager;

    public static SystemGroup from(String name, AbstractSystem... systems) {
        SystemGroup systemGroup = new SystemGroup(name);
        for (AbstractSystem system: systems) {
            systemGroup.insert(system);
        }
        return systemGroup;
    }

    private SystemGroup(String name) {
        this.name = name;
    }

    public void init(Manager manager) {
        this.manager = manager;
        for (AbstractSystem system: systems) {
            SystemGroup.injectSystemFields(manager, system);
        }
    }

    public void insert(AbstractSystem system) {
        if (iterating) throw new IllegalStateException("Systems can't be inserted while group is iterating");
        if (manager != null) SystemGroup.injectSystemFields(manager, system);
        this.systemGraph.insert(system);
        this.systems = this.systemGraph.getOrderedList();
    }

    public void execute() {
        this.iterating = true;
        for (AbstractSystem system: systems) {
            system.execute();
        }
        this.iterating = false;
    }

    public String toString() {
        return ToStringBuilder.fromC("[" + name + "]")
                .withArrayB(this.systems)
                .toString();
    }

}
