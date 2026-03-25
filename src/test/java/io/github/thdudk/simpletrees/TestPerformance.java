package io.github.thdudk.simpletrees;

import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.Consumer;

public class TestPerformance {
    public static void main(String[] args) throws IOException {
        performanceBenchmark(generateTrees(5000, 15), new BufferedWriter(new OutputStreamWriter(System.out)));
    }

    public static void performanceBenchmark(List<Tree<Integer>> trees, BufferedWriter output) throws IOException {
        var duplicate = benchmark(trees, Tree::duplicate, false);
        output.write("duplicate — average: " + duplicate.getAverage() + "ms, min: " + duplicate.getMin() + "ms, max: " + duplicate.getMax() + "ms\n");

        var subtree = benchmark(trees, tree -> tree.root().addSubtree(tree), true);
        output.write("addSubtree — average: " + subtree.getAverage() + "ms, min: " + subtree.getMin() + "ms, max: " + subtree.getMax() + "ms\n");

        output.flush();
    }
    public static DoubleSummaryStatistics benchmark(List<Tree<Integer>> trees, Consumer<Tree<Integer>> method, boolean mutating) throws IOException {
        var durations = new ArrayList<Double>();

        for(Tree<Integer> tree : trees) {
            var treeToUse = (mutating) ? tree.duplicate() : tree;
            durations.add(duration(() -> method.accept(treeToUse)));
        }

        return durations.stream().mapToDouble(Double::doubleValue).summaryStatistics();
    }
    public static double duration(Runnable runnable) throws IOException {
        double start = System.currentTimeMillis();
        runnable.run();
        double end = System.currentTimeMillis();

        return end - start;
    }

    public static List<Tree<Integer>> generateTrees(int numNodes, int numTrees) {
        // generates test trees
        List<Tree<Integer>> trees = new ArrayList<>();

        for(int j = 1; j <= numTrees; j++) {

            Tree<Integer> tree = Tree.ofRoot(1);

            for (int i = 0; i < numNodes; i++) {
                int randIdx = (int) (Math.random() * tree.nodes().size());
                var randomNode = tree.nodes().stream().toList().get(randIdx);
                randomNode.addChild(i);
            }

            trees.add(tree);
        }
        return trees;
    }
}
