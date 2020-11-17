package ecsploit.ecs.core;

import ecsploit.utils.debug.ToStringBuilder;

public final class SystemGroup extends ExecuteSystem {

    private final String name;
    private final Manager manager;
    private ExecuteSystem[] systems = new ExecuteSystem[]{};
    private final SystemGraph systemGraph = new SystemGraph();

    private boolean iterating = false;

    SystemGroup(String name, Manager manager) {
        this.name = name;
        this.manager = manager;
    }

    public void insert(ExecuteSystem system) {
        if (iterating) throw new IllegalStateException("Systems can't be inserted while group is iterating");
        this.systemGraph.insert(system);
        this.systems = this.systemGraph.getOrderedList();
    }

    public void execute() {
        this.iterating = true;
        for (ExecuteSystem system: systems) {
            this.manager.getComponentManager().setToDeferredStrategy();
            system.execute();
            this.manager.getComponentManager().clean();
            this.manager.getComponentManager().setToImmediateStrategy();
        }
        this.iterating = false;
    }

    public String toString() {
        return ToStringBuilder.fromC("[" + name + "]")
                .withArrayB(this.systems)
                .toString();
    }

}
