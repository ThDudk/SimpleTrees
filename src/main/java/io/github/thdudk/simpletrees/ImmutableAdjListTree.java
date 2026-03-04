package io.github.thdudk.simpletrees;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.NonNull;

import javax.naming.OperationNotSupportedException;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;
import java.util.Map;

public final class ImmutableAdjListTree<T> extends AdjListTree<T>{
    public ImmutableAdjListTree(Tree<T> tree) {
        super(tree.root().data());

        var iterator = tree.iterator();
        Map<Node<T>, Node<T>> inputToThis = new HashMap<>();
        inputToThis.put(tree.root(), root());

        iterator.next();

        while(iterator.hasNext()) {
            var curr = iterator.next();

            var thisNode = super.addChild(inputToThis.get(curr.parent()), curr.data());
            inputToThis.put(curr, thisNode);
        }
    }

    @Override
    public Node<T> addChild(@NonNull Node<T> parent, @NonNull T data) {
        throw new UnsupportedOperationException("Cannot remove child from immutable tree");
    }

    @Override
    public void removeChild(@NonNull Node<T> parent, @NonNull Node<T> child) {
        throw new UnsupportedOperationException("Cannot remove child from immutable tree");
    }
}
