package io.github.thdudk.simpletrees;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class ImmutableAdjListTree<T> extends AdjListTree<T>{
    public ImmutableAdjListTree(Tree<T> other) {
        super(other.root().data());

        DfsTreeIterator<T> iterator = new DfsTreeIterator<>(other);
        Map<Node<T>, Node<T>> otherToThis = new HashMap<>();
        otherToThis.put(other.root(), root());

        iterator.next();

        while(iterator.hasNext()) {
            var curr = iterator.next();

            Node<T> parent = otherToThis.get(curr.parent());
            var thisNode = super.addChild(parent, curr.data(), parent.childCount());
            otherToThis.put(curr, thisNode);
        }
    }

    @Override
    public Node<T> addChild(@NonNull Node<T> parent, @NonNull T data, int childIdx) {
        throw new UnsupportedOperationException("Cannot modify immutable tree");
    }

    @Override
    public Tree<T> removeChild(@NonNull Node<T> parent, @NonNull Node<T> child) {
        throw new UnsupportedOperationException("Cannot modify immutable tree");
    }
}
