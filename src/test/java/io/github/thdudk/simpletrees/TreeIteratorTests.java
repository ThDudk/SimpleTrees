package io.github.thdudk.simpletrees;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

class TreeIteratorTests {
    static List<Iterator<Tree.Node<Integer>>> binaryTreeIterators() {
        var tree = new ObjectMapper().readValue(new File("src/test/resources/perfectBinaryTree.json"), new TypeReference<Tree<Integer>>() {});

        return List.of(
            new BfsTreeIterator<>(tree),
            new DfsTreeIterator<>(tree)
        );
    }
    static List<Iterator<Tree.Node<Integer>>> subtreeIterators() {
        var tree = new ObjectMapper().readValue(new File("src/test/resources/perfectBinaryTree.json"), new TypeReference<Tree<Integer>>() {});

        return List.of(
            new BfsTreeIterator<>(tree.anyNodeWithData(2).orElseThrow()),
            new DfsTreeIterator<>(tree.anyNodeWithData(2).orElseThrow())
        );
    }

//    @ParameterizedTest
//    @MethodSource("binaryTreeIterators")
//    void testIncludes
}