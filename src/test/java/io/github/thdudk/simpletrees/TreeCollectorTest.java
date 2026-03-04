package io.github.thdudk.simpletrees;

import org.junit.jupiter.api.Test;

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
}