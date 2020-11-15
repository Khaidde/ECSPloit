package ecsploit.ecs.core;

import ecsploit.utils.debug.ToStringBuilder;

public final class SystemGroup extends ExecuteSystem {

    private final String name;
    private ExecuteSystem[] systems = new ExecuteSystem[]{};
    private final SystemGraph systemGraph = new SystemGraph();

    private boolean iterating = false;

    public static SystemGroup from(String name, ExecuteSystem... systems) {
        SystemGroup systemGroup = new SystemGroup(name);
        for (ExecuteSystem system: systems) {
            systemGroup.insert(system);
        }
        return systemGroup;
    }

    private SystemGroup(String name) {
        this.name = name;
    }

    public void insert(ExecuteSystem system) {
        if (iterating) throw new IllegalStateException("Systems can't be inserted while group is iterating");
        this.systemGraph.insert(system);
        this.systems = this.systemGraph.getOrderedList();
    }

    public void execute() {
        this.iterating = true;
        for (ExecuteSystem system: systems) {
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
