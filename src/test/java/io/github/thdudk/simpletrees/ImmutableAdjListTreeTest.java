package io.github.thdudk.simpletrees;

import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ImmutableAdjListTreeTest {
    @Test void isImmutable() {
        Tree<Integer> tree = new ObjectMapper().readValue(new File("src/test/resources/perfectBinaryTree.json"), new TypeReference<Tree<Integer>>() {})
            .immutable();

        assertAll(
            () -> assertThrows(UnsupportedOperationException.class, () -> tree.addChild(tree.anyNodeWithData(3).orElseThrow(), 2)),
            () -> assertThrows(UnsupportedOperationException.class, () -> tree.removeChild(tree.anyNodeWithData(4).orElseThrow(), tree.anyNodeWithData(2).orElseThrow())),
            () -> assertThrows(UnsupportedOperationException.class, () -> tree.anyNodeWithData(3).orElseThrow().addChild(2)),
            () -> assertThrows(UnsupportedOperationException.class, () -> tree.anyNodeWithData(4).orElseThrow().removeChild(tree.anyNodeWithData(2).orElseThrow())),
            () -> assertThrows(UnsupportedOperationException.class, () -> tree.anyNodeWithData(4).orElseThrow().addSubtree(tree))
        );
    }
}