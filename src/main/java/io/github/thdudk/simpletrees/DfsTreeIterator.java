package io.github.thdudk.simpletrees;

import java.util.*;

public class DfsTreeIterator<T> implements Iterator<Tree.Node<T>> {
    private final Deque<Tree.Node<T>> nodeStack = new LinkedList<>();

    public DfsTreeIterator(Tree<T> tree) {
        this(tree.root());
    }
    public DfsTreeIterator(Tree.Node<T> root) {
        nodeStack.add(root);
    }

    @Override
    public boolean hasNext() {
        return !nodeStack.isEmpty();
    }

    @Override
    public Tree.Node<T> next() {
        Tree.Node<T> curr = nodeStack.removeLast();

        var children = new ArrayList<>(curr.children());
        // reversed is needed to preserve the children's order (so leftmost child is visited first).
        Collections.reverse(children);

        nodeStack.addAll(children);

        return curr;
    }
}
