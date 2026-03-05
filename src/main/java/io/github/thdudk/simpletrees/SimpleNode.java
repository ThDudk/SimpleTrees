package io.github.thdudk.simpletrees;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NonNull;

import java.util.Objects;

public record SimpleNode<T>(@NonNull @JsonIgnore Tree<T> tree, @NonNull T data, @JsonIgnore int id) implements Tree.Node<T> {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SimpleNode<?> that = (SimpleNode<?>) o;
        return id == that.id && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, id);
    }

    @Override
    public String toString() {
        return "SimpleNode{" +
            "data=" + data +
            ", id=" + id +
            '}';
    }
}
