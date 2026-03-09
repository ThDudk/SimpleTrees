package io.github.thdudk.simpletrees;

import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TreeCollectorTest {
    @Test
    void catchesMultipleRoots() {
        var multiRootStream = Stream.of(
            new Tree.StreamEdge<>(
                Optional.of(new Tree.StreamNode<>(1, 1)),
                new Tree.StreamNode<>(2, 2)
            ),
            new Tree.StreamEdge<>(
                Optional.of(new Tree.StreamNode<>(3, 3)),
                new Tree.StreamNode<>(4, 4)
            )
        );

        assertThrows(RuntimeException.class, () -> multiRootStream.collect(new TreeCollector<>()));
    }
    @Test
    void catchesMultipleEdgesPointingToSameNode() {
        var multiRootStream = Stream.of(
            new Tree.StreamEdge<>(
                Optional.of(new Tree.StreamNode<>(0, 0)),
                new Tree.StreamNode<>(1, 1)
            ),
            new Tree.StreamEdge<>(
                Optional.of(new Tree.StreamNode<>(0, 0)),
                new Tree.StreamNode<>(3, 3)
            ),
            new Tree.StreamEdge<>(
                Optional.of(new Tree.StreamNode<>(1, 1)),
                new Tree.StreamNode<>(2, 2)
            ),
            new Tree.StreamEdge<>(
                Optional.of(new Tree.StreamNode<>(3, 3)),
                new Tree.StreamNode<>(2, 2)
            )
        );

        assertThrows(RuntimeException.class, () -> multiRootStream.collect(new TreeCollector<>()));
    }
    @Test
    void catchesDuplicateEdges() {
        var multiRootStream = Stream.of(
            new Tree.StreamEdge<>(
                Optional.of(new Tree.StreamNode<>(0, 0)),
                new Tree.StreamNode<>(1, 1)
            ),
            new Tree.StreamEdge<>(
                Optional.of(new Tree.StreamNode<>(1, 1)),
                new Tree.StreamNode<>(2, 2)
            ),
            new Tree.StreamEdge<>(
                Optional.of(new Tree.StreamNode<>(1, 1)),
                new Tree.StreamNode<>(2, 2)
            )
        );

        assertThrows(RuntimeException.class, () -> multiRootStream.collect(new TreeCollector<>()));
    }
    @Test
    void catchesDuplicateIds() {
        var multiRootStream = Stream.of(
            new Tree.StreamEdge<>(
                Optional.of(new Tree.StreamNode<>(0, 0)),
                new Tree.StreamNode<>(1, 1)
            ),
            new Tree.StreamEdge<>(
                Optional.of(new Tree.StreamNode<>(0, 0)),
                new Tree.StreamNode<>(1, 2) // duplicate ID
            )
        );

        assertThrows(RuntimeException.class, () -> multiRootStream.collect(new TreeCollector<>()));
    }

    @Test
    void mappingWorks() {
        var tree = new ObjectMapper().readValue(new File("src/test/resources/perfectBinaryTree.json"), new TypeReference<Tree<Integer>>() {});

        var result = tree.stream().map(edge -> new Tree.StreamEdge<>(
                edge.parent().map(
                    parent -> parent.mapData(d -> Integer.toString(parent.data()))
                ),
                edge.child().mapData(d -> Integer.toString((edge.child().data())))
            )
        ).collect(Tree.collector());

        var expected = Tree.ofRoot("4").root()
            .addChild("2")
                .addChild("1").parent()
                .addChild("3").parent()
                .parent()
            .addChild("6")
                .addChild("5").parent()
                .addChild("7").parent()
            .tree();

        assertAll(
            () -> assertTrue(treesEqual(expected, result))
        );
    }

    @Test
    void mappingSingleItem() {
        var tree = Tree.ofRoot(1);

        var newTree = tree.stream().map(
            edge -> edge.mapNodes(d -> Integer.toString(d))
        ).collect(Tree.collector());

        assertTrue(treesEqual(newTree, Tree.ofRoot("1")));
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
}