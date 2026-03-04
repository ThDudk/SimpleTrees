package io.github.thdudk.simpletrees;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.ToString;

import java.util.*;

import static java.util.Collections.unmodifiableCollection;

@ToString
public class AdjListTree<T> implements Tree<T> {
    private final Node<T> rootNode;
    private final Map<Node<T>, List<Node<T>>> adjList;
    private int lastNodeId = 1;

    protected static final class SerializedNode<T> {
        @JsonProperty("data") T data;
        @JsonProperty("children") List<SerializedNode<T>> children;
    }

    @JsonCreator
    protected AdjListTree(SerializedNode<T> root) {
        this(root.data);

        Map<SerializedNode<T>, Node<T>> serializedNodeToParent = new HashMap<>();
        var stack = new LinkedList<SerializedNode<T>>();

        root.children.forEach(child -> {
            stack.add(child);
            serializedNodeToParent.put(child, this.root());
        });

        while (!stack.isEmpty()) {
            var curr = stack.removeFirst();

            var nodeInThis = addChild(serializedNodeToParent.get(curr), curr.data);
            for (var child : curr.children) {
                stack.add(child);
                serializedNodeToParent.put(child, nodeInThis);
            }
        }
    }

    public AdjListTree(T root) {
        this.rootNode = new SimpleNode<>(this, root, 1);
        adjList = new HashMap<>();
        adjList.put(rootNode, new ArrayList<>());
    }

    private void requireInside(Node<T> node) throws IllegalArgumentException {
        if (node instanceof SimpleNode<T> && adjList.containsKey(node))
            return;

        throw new IllegalArgumentException("Node not contained in this tree. No such node: " + node);
    }

    @SafeVarargs
    private void requireInside(Node<T>... nodes) throws IllegalArgumentException {
        for (var node : nodes) {
            requireInside(node);
        }
    }

    @Override
    public Node<T> root() {
        return rootNode;
    }

    @Override
    public Node<T> parent(@NonNull Node<T> node) throws IllegalArgumentException {
        requireInside(node);

        for (var entry : adjList.entrySet()) {
            if (entry.getValue().contains(node)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public List<Node<T>> children(@NonNull Node<T> node) {
        requireInside(node);

        return Collections.unmodifiableList(adjList.get(node));
    }

    @Override
    public boolean hasChild(@NonNull Node<T> parent, @NonNull Node<T> child) {
        requireInside(parent, child);
        return adjList.get(parent).contains(child);
    }

    @Override
    public Node<T> addChild(@NonNull Node<T> parent, @NonNull T data) {
        requireInside(parent);

        SimpleNode<T> node = new SimpleNode<>(this, data, Math.addExact(lastNodeId, 1));
        lastNodeId += 1;
        adjList.put(node, new ArrayList<>());
        adjList.get(parent).add(node);
        return node;
    }

    @Override
    public void removeChild(@NonNull Node<T> parent, @NonNull Node<T> child) {
        requireInside(parent, child);
        if (!hasChild(parent, child))
            throw new IllegalArgumentException("parent: " + parent + ", has no such child: " + child);

        Deque<Node<T>> childrenToRemove = new LinkedList<>();
        childrenToRemove.add(child);

        while (!childrenToRemove.isEmpty()) {
            Node<T> toRemove = childrenToRemove.pop();

            childrenToRemove.addAll(adjList.get(toRemove));

            adjList.get(toRemove.parent()).remove(child);
        }
    }

    @Override
    public Collection<Node<T>> nodes() {
        return unmodifiableCollection(adjList.keySet());
    }

    @Override
    public Tree<T> subtree(@NonNull Node<T> subtreeRoot) throws IllegalArgumentException {
        requireInside(subtreeRoot);

        AdjListTree<T> subtree = new AdjListTree<>(subtreeRoot.data());

        BfsTreeIterator<T> iterator = new BfsTreeIterator<>(subtreeRoot);
        Map<Node<T>, Node<T>> treeToSubtree = new HashMap<>();
        treeToSubtree.put(subtreeRoot, subtree.root());

        iterator.next(); // root

        while (iterator.hasNext()) {
            var curr = iterator.next();

            var subtreeNode = subtree.addChild(treeToSubtree.get(curr.parent()), curr.data());
            treeToSubtree.put(curr, subtreeNode);
        }

        return subtree;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AdjListTree<?> that = (AdjListTree<?>) o;
        return Objects.equals(adjList, that.adjList);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(adjList);
    }
}
