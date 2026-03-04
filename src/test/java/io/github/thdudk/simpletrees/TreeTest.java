package io.github.thdudk.simpletrees;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.*;

class TreeTest {
    static List<Tree<Integer>> binaryTrees() {
        ObjectMapper mapper = new ObjectMapper();

        return List.of(
            mapper.readValue(new File("src/test/resources/perfectBinaryTree.json"), new TypeReference<Tree<Integer>>() {})
        );
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void nodes(Tree<Integer> tree) {
        assertEquals(Set.of(1, 2, 3, 4, 5, 6, 7), tree.nodes().stream().map(Tree.Node::data).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void throwsWhenInvalidNodeIsInput(Tree<Integer> tree) {
        Tree.Node<Integer> outsideNode = new Tree.Node<>() {
            @Override public int id() {return 1;}
            @Override public Tree<Integer> tree() {return null;}
            @Override public Integer data() {return 56;}
        };

        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> tree.parent(outsideNode)),
            () -> assertThrows(IllegalArgumentException.class, () -> tree.children(outsideNode)),
            () -> assertThrows(IllegalArgumentException.class, () -> tree.hasChild(tree.root(), outsideNode)),
            () -> assertThrows(IllegalArgumentException.class, () -> tree.hasChild(outsideNode, tree.root())),
            () -> assertThrows(IllegalArgumentException.class, () -> tree.addChild(outsideNode, 10)),
            () -> assertThrows(IllegalArgumentException.class, () -> tree.removeChild(tree.root(), outsideNode)),
            () -> assertThrows(IllegalArgumentException.class, () -> tree.removeChild(outsideNode, tree.root())),
            () -> assertThrows(IllegalArgumentException.class, () -> tree.removeChild(outsideNode, outsideNode)),
            () -> assertThrows(IllegalArgumentException.class, () -> tree.depth(outsideNode)),
            () -> assertThrows(IllegalArgumentException.class, () -> tree.isRoot(outsideNode)),
            () -> assertThrows(IllegalArgumentException.class, () -> tree.isLeaf(outsideNode)),
            () -> assertThrows(IllegalArgumentException.class, () -> tree.subtree(outsideNode))
        );
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void parent(Tree<Integer> tree) {
        tree.nodes().forEach(node ->
            node.children().forEach(child ->
                assertEquals(node, child.parent())));
    }
    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void children(Tree<Integer> tree) {
        assertArrayEquals(List.of(2, 6).toArray(), tree.children(tree.root()).stream().map(Tree.Node::data).toArray());
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void hasChild(Tree<Integer> tree) {
        var children = tree.root().children();
        var notChildren = tree.nodes().stream().filter(a -> !children.contains(a)).toList();

        children.forEach(child -> assertTrue(tree.root().hasChild(child)));
        notChildren.forEach(child -> assertFalse(tree.root().hasChild(child)));
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void addChild(Tree<Integer> tree) {
        tree.addChild(tree.root(), 10);
        assertTrue(tree.root().children().stream().map(Tree.Node::data).toList().contains(10));
    }
    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void canAddMultipleNodesWithTheSameData(Tree<Integer> tree) {
        tree.addChild(tree.root(), 10);
        tree.addChild(tree.root(), 10);

        assertEquals(2, tree.root()
            .children()
            .stream()
            .map(Tree.Node::data)
            .collect(groupingBy(o -> o, counting()))
            .get(10));
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void removeChild(Tree<Integer> tree) {
        Tree.Node<Integer> two = tree.anyNodeWithData(2).orElseThrow();
        tree.removeChild(tree.root(), two);

        assertEquals(0, tree.root()
            .children()
            .stream()
            .map(Tree.Node::data)
            .collect(groupingBy(o -> o, counting()))
            .getOrDefault(2, 0L));
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void depth(Tree<Integer> tree) {
        assertAll(
            () -> assertEquals(0, tree.depth(tree.root())),
            () -> assertEquals(1, tree.depth(tree.root().children().getFirst())),
            () -> assertEquals(2, tree.depth(tree.root().children().getFirst().children().getFirst()))
        );
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void subtree(Tree<Integer> tree) {
        Tree.Node<Integer> two = tree.anyNodeWithData(2).orElseThrow();

        Tree<Integer> subtree = Tree.ofRoot(2).root()
                .addChild(1).parent()
                .addChild(3).tree();

        assertTrue(treesEqual(subtree, two.subtree()));
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void prettyString(Tree<Integer> tree) {
        assertAll(
            () -> assertEquals(
                """
                    ├─ 4
                    │  ├─ 2
                    │  │  ├─ 1
                    │  │  ├─ 3
                    │  ├─ 6
                    │  │  ├─ 5
                    │  │  ├─ 7""",
                tree.prettyString()
            ),
            () -> assertEquals(
                """
                ├─ 4
                │  ├─ 2
                │  │  ├─ 1
                │  │  ├─ 3
                │  ├─ 6
                │  │  ├─ 5
                │  │  ├─ 7
                │  │  │  ├─ 9""",
                tree.anyNodeWithData(7).orElseThrow().addChild(9).tree().prettyString()
            )
        );
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void mapToDifferentTypeUsingStream(Tree<Integer> tree) {
        Tree<Integer> mappedToOneHundredMinusValue = tree.stream()
            .map(edge -> edge.mapNodes(data -> 100 - data))
            .collect(Tree.collector());

        var expected = new ObjectMapper().readValue(new File("src/test/resources/invertedBinaryTree.json"), new TypeReference<Tree<Integer>>(){});

        assertTrue(treesEqual(expected, mappedToOneHundredMinusValue));
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void iteratorActsLikeDfsIterator(Tree<Integer> tree) {
        var iterator = tree.iterator();
        var dfsIterator = new DfsTreeIterator<>(tree);

        while(iterator.hasNext()) {
            assertEquals(dfsIterator.next(), iterator.next());
        }
    }

    public <T> boolean treesEqual(Tree<T> first, Tree<T> second) {
        if(first.nodes().size() != second.nodes().size()) return false;

        var firstIterator = first.iterator();
        var secondIterator = second.iterator();

        while(firstIterator.hasNext()) {
            if(firstIterator.next().data() != secondIterator.next().data()) return false;
        }
        return true;
    }
}