package io.github.thdudk.simpletrees;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public class BfsTreeIterator<T> implements Iterator<Tree.Node<T>> {
    private final Deque<Tree.Node<T>> nodeQueue = new LinkedList<>();

    public BfsTreeIterator(Tree<T> tree) {
        this(tree.root());
    }
    public BfsTreeIterator(Tree.Node<T> root) {
        nodeQueue.add(root);
    }

    @Override
    public boolean hasNext() {
        return !nodeQueue.isEmpty();
    }

    @Override
    public Tree.Node<T> next() {
        Tree.Node<T> curr = nodeQueue.removeFirst();
        nodeQueue.addAll(curr.children());

        return curr;
    }
}
