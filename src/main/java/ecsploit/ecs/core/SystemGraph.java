package ecsploit.ecs.core;

import ecsploit.ecs.injection.ExecuteAfter;
import ecsploit.ecs.injection.ExecuteBefore;
import ecsploit.utils.collections.DenseList;

import java.util.*;

public class SystemGraph {

    private static class SystemNode {

        private final int id;
        private int mark = 0;

        private ExecuteSystem system;

        private final DenseList<SystemNode> nextNodes = new DenseList<>(5);

        SystemNode(int id) {
            this.id = id;
        }

        public void addNextNode(SystemNode nextNode) {
            this.nextNodes.add(nextNode);
            nextNode.updateMarkFrom(this.mark, this);
        }

        private static class IllegalCycleException extends RuntimeException {
            public IllegalCycleException(String msg) {
                super(msg);
            }
        }

        private void updateMarkFrom(int prevNodeMark, SystemNode initialInsertNode) {
            if (prevNodeMark >= mark) {
                if (initialInsertNode.id == id) {
                    throw new IllegalCycleException("Systems form a cyclic dependency chain! Check paths related to "
                            + initialInsertNode.system.getClass().getSimpleName() + ".class");
                }
                this.mark = prevNodeMark + 1;
                nextNodes.forEach(next -> next.updateMarkFrom(this.mark, initialInsertNode));
            }
        }
    }

    private Map<Class<? extends ExecuteSystem>, SystemNode> systemNodes = new HashMap<>();

    private SystemNode getOrCreateNode(Class<? extends ExecuteSystem> systemClass) {
        return systemNodes.computeIfAbsent(systemClass, key -> new SystemNode(systemNodes.size()));
    }

    public void insert(ExecuteSystem system) {
        Class<? extends ExecuteSystem> systemClass = system.getClass();
        SystemNode insertNode = this.getOrCreateNode(systemClass);
        insertNode.system = system;

        if (systemClass.isAnnotationPresent(ExecuteAfter.class)) {
            Class<? extends ExecuteSystem>[] afterSystems = systemClass.getAnnotation(ExecuteAfter.class).value();
            for (Class<? extends ExecuteSystem> afterSystem: afterSystems) {
                SystemNode other = this.getOrCreateNode(afterSystem);
                other.addNextNode(insertNode);
            }
        }
        if (systemClass.isAnnotationPresent(ExecuteBefore.class)) {
            Class<? extends ExecuteSystem>[] beforeSystems = systemClass.getAnnotation(ExecuteBefore.class).value();
            for (Class<? extends ExecuteSystem> beforeSystem: beforeSystems) {
                SystemNode other = this.getOrCreateNode(beforeSystem);
                insertNode.addNextNode(other);
            }
        }
    }

    public ExecuteSystem[] getOrderedList() {
        List<SystemNode> listOfNodes = new ArrayList<>();
        for (SystemNode node: systemNodes.values()) {
            if (node.system != null) listOfNodes.add(node);
        }
        listOfNodes.sort(Comparator.comparingInt(a -> a.mark));

        int counter = 0;
        ExecuteSystem[] orderedList = new ExecuteSystem[listOfNodes.size()];
        for (SystemNode node: listOfNodes) {
            orderedList[counter++] = node.system;
        }
        return orderedList;
    }
}
