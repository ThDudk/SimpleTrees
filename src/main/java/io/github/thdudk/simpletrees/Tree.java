package io.github.thdudk.simpletrees;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/// Represents a tree data structure.
///
/// # Example
///
/// ```
/// Tree<Integer> binaryTree = Tree.ofRoot(4).root()
///     .addChild(2)
///         .addChild(1).parent()
///         .addChild(3).parent()
///         .parent()
///     .addChild(6)
///         .addChild(5).parent()
///         .addChild(7).tree();
///
/// System.out.println(binaryTree.prettyString());
/// // output:
/// // ├─ 4
/// // │  ├─ 2
/// // │  │  ├─ 1
/// // │  │  ├─ 3
/// // │  ├─ 6
/// // │  │  ├─ 5
/// // │  │  ├─ 7
/// ```
///
/// @param <T> Type of data contained in `this`
@JsonSerialize(using = TreeSerializer.class)
@JsonDeserialize(as = AdjListTree.class)
public interface Tree<T> extends Iterable<Tree.Node<T>> {
    /// Represents a node in a tree. These are a reflection of a tree's nodes and all operations performed on a node are performed on the original tree.
    ///
    /// methods on node should only perform operations through their tree. As such it is not recommended that default operations be overridden under any circumstances.
    interface Node<T> extends Iterable<Node<T>>{
        /// Integer id of the node. Ids will always be greater for nodes added later.
        ///
        /// This means that, for any `node` in `this`, the children of that node will have increasing Ids.
        int id();
        Tree<T> tree();
        @JsonSerialize T data();
        @JsonSerialize default List<Node<T>> children() {
            return tree().children(this);
        }
        default Node<T> parent() {
            return tree().parent(this);
        }

        /// alias for {@link Tree#isRoot(Node) tree().isRoot(this)}
        @JsonIgnore default boolean isRoot() {return tree().isRoot(this);}
        /// alias for {@link Tree#isLeaf(Node) tree().isLeaf(this)}
        @JsonIgnore default boolean isLeaf() {return tree().isLeaf(this);}

        /// alias for {@link Tree#hasChild(Node, Node) tree().hasChild(this, child)}
        default boolean hasChild(Node<T> child) { return tree().hasChild(this, child); }
        /// alias for {@link Tree#addChild(Node, Object) tree().addChild(this, childData)}
        default Node<T> addChild(T childData) { return tree().addChild(this, childData); }
        /// alias for {@link Tree#removeChild(Node, Node) tree().removeChild(this, child)}
        default void removeChild(Node<T> child) { tree().removeChild(this, child); }
        /// alias for {@link Tree#subtree(Node) tree().subtree(this)}
        default Tree<T> subtree() { return tree().subtree(this); }
        /// alias for {@link Tree#depth(Node) tree().depth(this)}
        default int depth() { return tree().depth(this); }

        @Override @NotNull
        default Iterator<Node<T>> iterator() {
            return new DfsTreeIterator<>(this);
        }
    }
    record StreamNode<T>(int id, T data){
        public <N> StreamNode<N> mapData(Function<T, N> mapper) {
            return new StreamNode<>(id, mapper.apply(data()));
        }

        static <T> StreamNode<T> of(Node<T> node) {
            return new StreamNode<>(node.id(), node.data());
        }
    }
    record StreamEdge<T>(Optional<StreamNode<T>> parent, StreamNode<T> child){
        /// maps the data in `parent` and `child` according to the provided `mapper` function.
        ///
        /// @param mapper the mapper function
        /// @param <N> the new data type
        public <N> StreamEdge<N> mapNodes(Function<T, N> mapper) {
            return new StreamEdge<>(
                parent.map(p -> p.mapData(mapper)),
                child.mapData(mapper)
            );
        }
    }

    Collection<Node<T>> nodes();
    /// There is no guarantee which `node` will be returned.
    /// Additionally, there is no guarantee that the result will be consistent.
    ///
    /// @return any node in `this` with the given `data` or empty if there is no node in `this` with `data`.
    /// @see #nodesWithData(T data)
    default Optional<Node<T>> anyNodeWithData(@NonNull T data) {
        for(var node : nodes()){
            if(node.data().equals(data)) return Optional.of(node);
        }
        return Optional.empty();
    }
    /// @return all nodes in `this` with data matching `data`, or an empty collection if there are none.
    /// @see #anyNodeWithData(T data)
    default Collection<Node<T>> nodesWithData(@NonNull T data) {
        return nodes().stream().filter(n -> n.data().equals(data)).toList();
    }

    @JsonSerialize Node<T> root();
    /// @return the parent of `node`, or null if `node` is the root.
    /// @throws IllegalArgumentException if `node` is not contained in `this`.
    /// @see Node#parent()
    @Nullable
    Node<T> parent(@NonNull Node<T> node) throws IllegalArgumentException;
    /// @return a collection of node's children.
    /// @throws IllegalArgumentException if `node` is not contained in `this`.
    /// @see Node#children()
    List<Node<T>> children(@NonNull Node<T> node) throws IllegalArgumentException;
    /// @throws IllegalArgumentException if either `parent` or `child` are not contained in `this`.
    boolean hasChild(@NonNull Node<T> parent, @NonNull Node<T> child) throws IllegalArgumentException;
    /// Adds `data` as a child node to `parent`.
    /// @throws IllegalArgumentException if `parent` is not contained in `this`
    Node<T> addChild(@NonNull Node<T> parent,@NonNull T data) throws IllegalArgumentException;
    /// Removes `child` from `parent`.
    ///
    /// @throws IllegalArgumentException if either `parent` or `child` are not contained in `this`.
    void removeChild(@NonNull Node<T> parent, @NonNull Node<T> child) throws IllegalArgumentException;
    /// # Example
    /// ```
    /// ├─ 1
    /// │  ├─ 2
    /// │  ├─ 8
    /// ```
    /// In this case:
    /// - the depth of node "1" is 0.
    /// - the depth of nodes "2" and "8", are 1.
    ///
    /// @return The number of nodes above `node`.
    /// @throws IllegalArgumentException if node is not contained in this
    default int depth(@NonNull Node<T> node) throws IllegalArgumentException {
        int numParents = -1;
        Node<T> curr = node;

        while(curr != null) {
            numParents++;
            curr = parent(curr); // throws Exception if node not in `this`
        }

        return numParents;
    }

    /// @return true if `node` is the root node, aka `node` has no parent.
    default boolean isRoot(@NonNull Node<T> node) {return parent(node) == null;}
    /// @return true if `node` is a leaf node, aka `node` has no children.
    default boolean isLeaf(@NonNull Node<T> node) {return children(node).isEmpty();}

    /// Note: it is not guaranteed that node ids will be preserved.
    ///
    /// @return a copy of `this` with only nodes under (and including) `root`.
    Tree<T> subtree(@NonNull Node<T> root);

    /// Creates a Tree with `root`.
    static <T> Tree<T> ofRoot(@NonNull T root) {
        return new AdjListTree<>(root);
    }

    /// Returns a formatted string for the tree.
    ///
    /// # Example
    /// ```
    /// ├─ 4
    /// │  ├─ 2
    /// │  │  ├─ 1
    /// │  │  ├─ 3
    /// │  ├─ 6
    /// │  │  ├─ 5
    /// │  │  ├─ 7
    /// ```
    default String prettyString() {
        DfsTreeIterator<T> iterator = new DfsTreeIterator<>(this);
        StringBuilder builder = new StringBuilder();

        while (iterator.hasNext()) {
            var curr = iterator.next();

            int depth = depth(curr);

            if(depth > 0)
                builder.repeat("│  ", depth);

            builder.append("├─ ").append(curr.data());
            if(iterator.hasNext()) builder.append("\n");
        }
        return builder.toString();
    }
    /// Both classes {@link StreamEdge} and {@link StreamNode} are intended to be simple POJOs, which can be used in a stream.
    ///
    /// Note: The root node will be given a StreamEdge with no parent.
    /// This preserves the root node in cases where there is only 1 node.
    ///
    /// {@link StreamEdge#mapNodes(Function mapper)} preserves this empty parent.
    ///
    /// # Examples
    /// ```
    /// Tree<Integer> intTree = ...;
    ///
    /// Tree<String> strTree = intTree.stream()
    ///     .map(edge -> edge.mapNodes(Object::toString))
    ///     .collect(Tree.collector());
    /// ```
    ///
    /// @return a stream of `StreamEdges`
    default Stream<StreamEdge<T>> stream() {
        List<StreamEdge<T>> edges = new ArrayList<>();

        for(Node<T> node : this) {
            Node<T> parent = node.parent();
            edges.add(
                new StreamEdge<>(
                    (parent == null) ? Optional.empty() : Optional.of(StreamNode.of(parent)),
                    StreamNode.of(node)
                )
            );
        }

        return edges.stream();
    }
    /// The returned iterator will be of type {@link DfsTreeIterator}. This means nodes will be traversed depth first.
    ///
    /// If you want to create an iterator that traverses breadth first, see {@link BfsTreeIterator}
    ///
    /// @return a node iterator for `this`
    /// @see DfsTreeIterator
    /// @see BfsTreeIterator
    @Override @NotNull
    default Iterator<Node<T>> iterator() {
        return new DfsTreeIterator<>(this);
    }
    /// @return a collector used to convert a stream of {@link StreamEdge}s into a Tree.
    /// @see TreeCollector
    /// @see #stream()
    static <T> TreeCollector<T> collector() {
        return new TreeCollector<>();
    }

    default Tree<T> immutable() {
        return new ImmutableAdjListTree<>(this);
    }
}
