package io.github.thdudk.simpletrees;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collectors.*;

/// Collects a stream of {@link Tree.StreamEdge}s into a {@link Tree}.
///
/// The preserves the tree's order (so children will be in the same order as the original tree—assuming ids have not been modified)
///
/// # Example
///
/// ```
/// Tree<Integer> intTree = ...;
///
/// Tree<String> strTree = intTree.stream()
///     .map(edge -> edge.mapNodes(Object::toString))
///     .collect(new TreeCollector<>());
/// ```
/// @param <T> type of the data contained in the tree
public class TreeCollector<T> implements Collector<Tree.StreamEdge<T>, List<Tree.StreamEdge<T>>, Tree<T>> {
    private record ParentChildPair<T>(Tree.StreamNode<T> parent, Tree.StreamNode<T> child){}

    @Override
    public Supplier<List<Tree.StreamEdge<T>>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<Tree.StreamEdge<T>>, Tree.StreamEdge<T>> accumulator() {
        return List::add;
    }

    @Override
    public BinaryOperator<List<Tree.StreamEdge<T>>> combiner() {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    @Override
    public Function<List<Tree.StreamEdge<T>>, Tree<T>> finisher() {
        return (edges) -> {
            var possibleRoots = edges.stream().filter(a -> a.parent().isEmpty()).map(Tree.StreamEdge::child).toList();

            if(possibleRoots.isEmpty()) throw new RuntimeException("Found no roots.");
            if(possibleRoots.size() > 1) throw new RuntimeException("Found more than 1 root.");

            if(edges.size() == 1) return Tree.ofRoot(edges.get(0).child().data());

            var root = possibleRoots.get(0);
            Tree<T> tree = Tree.ofRoot(root.data());

            var adjList = edges.stream()
                .filter(a -> a.parent().isPresent())
                .map(a -> new ParentChildPair<>(a.parent().orElseThrow(), a.child()))
                .collect(groupingBy(
                    ParentChildPair::parent,
                    mapping(ParentChildPair::child, toList()))
                );

            adjList.replaceAll((node, children) ->
                children.stream().sorted(Comparator.comparingInt(Tree.StreamNode::id)).toList()
            );

            Map<Tree.StreamNode<T>, Tree.Node<T>> nodeMap = new HashMap<>();
            nodeMap.put(root, tree.root());

            Deque<ParentChildPair<T>> stack = new LinkedList<>(
                adjList.get(root).stream()
                    .map(rootChild -> new ParentChildPair<>(root, rootChild))
                    .sorted(Comparator.comparingInt(a -> a.child.id()))
                    .toList());

            while(!stack.isEmpty()) {
                var currPair = stack.removeFirst();

                for (var child : adjList.getOrDefault(currPair.child, Collections.emptyList())) {
                    stack.add(new ParentChildPair<>(currPair.child, child));
                }

                var currNode = tree.addChild(nodeMap.get(currPair.parent), currPair.child.data());
                nodeMap.put(currPair.child, currNode);
            }

            return tree;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
