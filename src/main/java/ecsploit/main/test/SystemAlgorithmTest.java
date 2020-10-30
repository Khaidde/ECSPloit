package ecsploit.main.test;

import ecsploit.utils.collections.DenseList;
import ecsploit.utils.debug.RandomUtils;
import ecsploit.utils.debug.ToStringBuilder;

import java.util.*;
import java.util.concurrent.TimeoutException;

public class SystemAlgorithmTest {

    private static class TestSystem {
        private String[] after = new String[]{};
        private String[] before = new String[]{};

        private final String className;

        public TestSystem(String className) {
            this.className = className;
        }

        public TestSystem after(String... after) {
            this.after = after;

//            this.after = new String[after.length];
//            for (int i = 0; i < after.length; i++) {
//                this.after[i] = "sys" + after[i];
//            }
            return this;
        }

        public TestSystem before(String... before) {
            this.before = before;

//            this.before = new String[before.length];
//            for (int i = 0; i < before.length; i++) {
//                this.before[i] = "sys" + before[i];
//            }
            return this;
        }

        public String toString() {
            return className;
        }
    }

    private static class SystemNode {

        private final int id;
        private int mark = 0;

        private TestSystem system;

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
                    throw new IllegalCycleException("Systems form a cyclic dependency chain! Check paths related to " + initialInsertNode);
                }
                this.mark = prevNodeMark + 1;
                nextNodes.forEach(next -> next.updateMarkFrom(this.mark, initialInsertNode));
            }
        }

        public String toString() {
            return ToStringBuilder
                    .fromC("SystemNode(" + (system == null ? "" : "name=" + system.className + ",") + "mark=" + mark + ")")
                    .toString();
        }
    }

    private static class SystemGraph {
        Map<String, SystemNode> systemNodes = new HashMap<>();

        SystemNode getOrCreateNode(String systemName) {
            return systemNodes.computeIfAbsent(systemName, key -> new SystemNode(systemNodes.size()));
        }

        void insert(TestSystem system) {
            SystemNode insertNode = this.getOrCreateNode(system.className);
            insertNode.system = system;
            for (String afterSystem: system.after) {
                SystemNode other = this.getOrCreateNode(afterSystem);
                other.addNextNode(insertNode);
            }
            for (String beforeSystem: system.before) {
                SystemNode other = this.getOrCreateNode(beforeSystem);
                insertNode.addNextNode(other);
            }
        }

        TestSystem[] getOrderedList() {
            List<SystemNode> listOfNodes = new ArrayList<>();
            for (SystemNode node: systemNodes.values()) {
                if (node.system != null) listOfNodes.add(node);
            }
            listOfNodes.sort(Comparator.comparingInt(a -> a.mark));

            int counter = 0;
            TestSystem[] orderedList = new TestSystem[listOfNodes.size()];
            for (SystemNode node: listOfNodes) {
                orderedList[counter++] = node.system;
            }
            return orderedList;
        }
    }

    private static class TestAlgorithm {
        SystemGraph graph = new SystemGraph();
        String[] orderedSystemNames = new String[]{};

        void insert(TestSystem system) {
            graph.insert(system);

            TestSystem[] orderedSystems = graph.getOrderedList();
            orderedSystemNames = new String[orderedSystems.length];
            for (int i = 0; i < orderedSystems.length; i++) {
                orderedSystemNames[i] = orderedSystems[i].toString();
            }
        }

        public String toString() {
            return ToStringBuilder.from(this)
                    .withMap("graph", graph.systemNodes)
                    .withStringArray("ordered", orderedSystemNames)
                    .toString();
        }
    }

    private static Random random = new Random();

    private static String name(int id) {
        return "sys" + id;
    }

    private static String[] generateDependencies(int N, int exclude) {
        int[] set = RandomUtils.randIntSetExclude(N, random.nextInt(N - 1) + 1, exclude);

        String[] dependencies = new String[set.length];
        for (int i = 0; i < set.length; i++) {
            dependencies[i] = name(set[i]);
        }
        return dependencies;
    }

    public static void main(String[] args) {

        TestAlgorithm algorithm = new TestAlgorithm();

        final int N = 10;
        final int N_DEPENDENCIES = 2;
        for (int i = 0; i < N; i++) {
            TestSystem testSystem = new TestSystem(name(i));
            if (random.nextDouble() < 0.25) {
                testSystem.after(generateDependencies(N_DEPENDENCIES, i));
            } else if (random.nextDouble() < 0.5) {
                testSystem.before(generateDependencies(N_DEPENDENCIES, i));
            }
            algorithm.insert(testSystem);
        }

//        TestSystem[] systems = new TestSystem[] {
//                new TestSystem("1").before("2"),
//                new TestSystem("2"),
//                new TestSystem("3").after("1"),
//                new TestSystem("4").before("3"),
//                new TestSystem("5").after("3"),
//                new TestSystem("6").after("5"),
//                new TestSystem("7").after("6", "4"),
//                new TestSystem("8").after("2").before("7"),
//                new TestSystem("9").after("2", "7"),
//                new TestSystem("10").after("7"),
//                new TestSystem("11").after("5").before("8"),
//                new TestSystem("12").after("3"),
//                new TestSystem("13").after("12"),
//                new TestSystem("14").after("13").before("2")
//
//        };
//        for (TestSystem system : systems) {
//            algorithm.insert(system);
//        }
        System.out.println(algorithm);

//        TestSystem b = new TestSystem("1b0").before("0b1");
//        algorithm.insert(b);
//        System.out.println(algorithm);
    }
}
