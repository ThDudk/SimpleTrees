package io.github.thdudk.simpletrees;

import io.github.thdudk.simpletrees.exceptions.NodeNotInTreeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.*;

class TreeTest {
    static List<Tree<Integer>> binaryTrees() {
        ObjectMapper mapper = new ObjectMapper();

        return List.of(
            mapper.readValue(new File("src/test/resources/perfectBinaryTree.json"), new TypeReference<Tree<Integer>>() {})
        );
    }
    static List<Tree<Integer>> possiblyImmutableBinaryTrees() {
        return binaryTrees().stream().<Tree<Integer>>mapMulti((tree, consumer) -> {
            consumer.accept(tree);
            consumer.accept(new ImmutableAdjListTree<>(tree));
        }).toList();
    }
    static List<Tree<Integer>> integerTrees() {
        return List.of(
            new AdjListTree<>(1)
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
            @Override public Tree<Integer> tree() {return null;}
            @Override public Integer data() {return 56;}
            @Override public int childCount() {return 3;}
        };

        assertAll(
            () -> assertThrows(NodeNotInTreeException.class, () -> tree.parent(outsideNode)),
            () -> assertThrows(NodeNotInTreeException.class, () -> tree.children(outsideNode)),
            () -> assertThrows(NodeNotInTreeException.class, () -> tree.hasChild(tree.root(), outsideNode)),
            () -> assertThrows(NodeNotInTreeException.class, () -> tree.hasChild(outsideNode, tree.root())),
            () -> assertThrows(NodeNotInTreeException.class, () -> tree.addChild(outsideNode, 10)),
            () -> assertThrows(NodeNotInTreeException.class, () -> tree.removeChild(tree.root(), outsideNode)),
            () -> assertThrows(NodeNotInTreeException.class, () -> tree.removeChild(outsideNode, tree.root())),
            () -> assertThrows(NodeNotInTreeException.class, () -> tree.removeChild(outsideNode, outsideNode)),
            () -> assertThrows(NodeNotInTreeException.class, () -> tree.depth(outsideNode)),
            () -> assertThrows(NodeNotInTreeException.class, () -> tree.isRoot(outsideNode)),
            () -> assertThrows(NodeNotInTreeException.class, () -> tree.isLeaf(outsideNode)),
            () -> assertThrows(NodeNotInTreeException.class, () -> tree.subtree(outsideNode))
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
    void addChildAtIndex0(Tree<Integer> tree) {
        tree.root().addChild(-5, 0);

        var children = tree.root().children().stream().map(Tree.Node::data).toArray();
        assertArrayEquals(new Integer[]{-5, 2, 6}, children);
    }
    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void addChildAtMiddleIndex(Tree<Integer> tree) {
        tree.root().addChild(-5, 1);

        var children = tree.root().children().stream().map(Tree.Node::data).toArray();
        assertArrayEquals(new Integer[]{2, -5, 6}, children);
    }
    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void addChildAtEndIndex(Tree<Integer> tree) {
        tree.root().addChild(-5, 2);

        var children = tree.root().children().stream().map(Tree.Node::data).toArray();
        assertArrayEquals(new Integer[]{2, 6, -5}, children);
    }
    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void addChildThrowsOutOfBoundsExceptionAtEndIndex(Tree<Integer> tree) {
        assertThrows(IndexOutOfBoundsException.class, () -> tree.root().addChild(3, 3));
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

        assertFalse(tree.nodes().stream().anyMatch(n -> n.data().equals(2)));
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void depth(Tree<Integer> tree) {
        assertAll(
            () -> assertEquals(0, tree.depth(tree.root())),
            () -> assertEquals(1, tree.depth(tree.root().children().get(0))),
            () -> assertEquals(2, tree.depth(tree.root().children().get(0).children().get(0)))
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
    void map(Tree<Integer> tree) {
        Tree<Integer> mappedToOneHundredMinusValue = tree.map(data -> 100 - data);

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

    @ParameterizedTest
    @MethodSource(value = "integerTrees")
    void addSubtree(Tree<Integer> tree) {
        tree.root()
            .addChild(2).parent()
            .addChild(3).tree();

        var three = tree.anyNodeWithData(3).orElseThrow();

        Tree<Integer> subtree = Tree.ofRoot(4).root()
            .addChild(5).parent()
            .addChild(6)
                .addChild(7).tree();

        tree.addSubtree(three, subtree);

        Tree<Integer> result = Tree.ofRoot(1).root()
            .addChild(2).parent()
            .addChild(3)
                .addChild(4)
                    .addChild(5).parent()
                    .addChild(6)
                        .addChild(7)
            .tree();

        assertTrue(treesEqual(result, tree));
    }
    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void addSubtreeAtIdx(Tree<Integer> tree) {
        Tree<Integer> subtree = Tree.ofRoot(4).root()
            .addChild(5).parent()
            .addChild(6)
            .addChild(7).tree();

        tree.root().addSubtree(subtree, 0);

        var result = Tree.ofRoot(4).root()
            .addChild(4)
                .addChild(5).parent()
                .addChild(6)
                    .addChild(7).parent()
                    .parent()
                .parent()
            .addChild(2)
                .addChild(1).parent()
                .addChild(3).parent()
                .parent()
            .addChild(6)
                .addChild(5).parent()
                .addChild(7).tree();

        System.out.println(result.prettyString());
        System.out.println(tree.prettyString());

        assertTrue(treesEqual(result, tree));
    }

    @ParameterizedTest
    @MethodSource(value = "integerTrees")
    void addingSelfAsSubtree(Tree<Integer> tree) {
        tree.root()
            .addChild(2).parent()
            .addChild(3).tree();

        tree.anyNodeWithData(3).orElseThrow()
            .addSubtree(tree);

        Tree<Integer> result = Tree.ofRoot(1).root()
            .addChild(2).parent()
            .addChild(3)
                .addChild(1)
                    .addChild(2).parent()
                    .addChild(3)
            .tree();

        assertTrue(treesEqual(result, tree));
    }

    @ParameterizedTest
    @MethodSource(value = "possiblyImmutableBinaryTrees")
    void adjListTreeCopy(Tree<Integer> tree) {
        var copy = new AdjListTree<>(tree);

        assertAll(
            () -> assertTrue(treesEqual(tree, copy)),
            () -> {
                for(var node : tree.nodes()) {
                    assertNotSame(copy.nodesWithData(node.data()), node);
                }
            }
        );
    }

    public <T> boolean treesEqual(Tree<T> first, Tree<T> second) {
        if(first.nodes().size() != second.nodes().size()) return false;

        var firstIterator = first.iterator();
        var secondIterator = second.iterator();

        while(firstIterator.hasNext()) {
            if(!firstIterator.next().data().equals(secondIterator.next().data())) return false;
        }
        return true;
    }

    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void trimInternalNode(Tree<Integer> tree) {
        tree.root().trim(tree.anyNodeWithData(2).orElseThrow());
        Object[] children = tree.root().children().stream().map(Tree.Node::data).toArray();
        System.out.println(Arrays.toString(children));

        assertAll(
            () -> assertArrayEquals(new Integer[]{1, 3, 6}, children)
        );
    }
    @ParameterizedTest
    @MethodSource(value = "binaryTrees")
    void trimLeafNode(Tree<Integer> tree) {
        tree.anyNodeWithData(2).orElseThrow().trim(tree.anyNodeWithData(3).orElseThrow());

        System.out.println(tree.prettyString());

        assertAll(
            () -> assertTrue(tree.anyNodeWithData(3).isEmpty())
        );
    }

    @Test
    void nodeEqualsTakesTreeIntoAccount() {
        var tree = Tree.ofRoot(1);
        var other = Tree.ofRoot(1);

        assertAll(
            () -> assertNotEquals(other.root(), tree.root()),
            () -> assertEquals(tree.root(), tree.root())
        );
    }
}