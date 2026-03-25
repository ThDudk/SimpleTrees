package io.github.thdudk.simpletrees;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.thdudk.simpletrees.exceptions.NodeNotInTreeException;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.*;
import java.util.function.Function;

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
public interface Tree<T> extends Iterable<Tree.Node<T>>, Cloneable {
    /// Represents a node in a tree. These are a reflection of a tree's nodes and all operations performed on a node are performed on the original tree.
    ///
    /// methods on node should only perform operations through their tree. As such it is not recommended that default operations be overridden under any circumstances.
    interface Node<T> extends Iterable<Node<T>>{
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
        /// alias for {@link Tree#idxOfChild(Node, Node) tree().idxOfChild(this, child)}
        default int idxOfChild(Node<T> child) {
            return tree().idxOfChild(this, child);
        }
        /// alias for {@link Tree#numChildren(Node) tree().numChildren(this)}
        default int childCount() {
            return tree().numChildren(this);
        }
        /// alias for {@link Tree#addChild(Node, Object, int) tree().addChild(this, childData, childIdx)}
        default Node<T> addChild(T childData, int childIdx) { return tree().addChild(this, childData, childIdx); }
        /// alias for {@link Tree#addChild(Node, Object) tree().addChild(this, childData)}
        default Node<T> addChild(T childData) { return tree().addChild(this, childData); }
        /// alias for {@link Tree#removeChild(Node, Node) tree().removeChild(this, child)}
        default Tree<T> removeChild(Node<T> child)  { return tree().removeChild(this, child); }
        /// alias for {@link Tree#subtree(Node) tree().subtree(this)}
        default Tree<T> subtree() { return tree().subtree(this); }
        /// alias for {@link Tree#depth(Node) tree().depth(this)}
        default int depth() { return tree().depth(this); }
        /// alias for {@link Tree#addSubtree(Node, Tree, int) tree().addSubtree(this, subtree, childIdx)}
        default void addSubtree(Tree<T> subtree, int childIdx) {
            tree().addSubtree(this, subtree, childIdx);
        }
        /// alias for {@link Tree#addSubtree(Node, Tree) tree().addSubtree(this, subtree)}
        default void addSubtree(Tree<T> subtree) {
            tree().addSubtree(this, subtree);
        }
        /// alias for {@link Tree#trim(Node, Node) tree().trim(this, child)}
        default void trim(Node<T> child) {tree().trim(this, child);}

        @Override @NotNull
        default Iterator<Node<T>> iterator() {
            return new DfsTreeIterator<>(this);
        }
    }

    Collection<Node<T>> nodes();
    @JsonSerialize Node<T> root();
    /// @return the parent of `node`, or null if `node` is the root.
    /// @throws NodeNotInTreeException if `node` is not contained in `this`.
    /// @see Node#parent()
    Node<T> parent(@NonNull Node<T> node);
    /// @return a collection of node's children.
    /// @throws NodeNotInTreeException if `node` is not contained in `this`.
    /// @see Node#children()
    List<Node<T>> children(@NonNull Node<T> node);
    /// @throws NodeNotInTreeException if either `parent` or `child` are not contained in `this`.
    boolean hasChild(@NonNull Node<T> parent, @NonNull Node<T> child);
    /// Adds `data` as a child node to `parent` at index `childIdx`.
    /// @param childIdx The index to insert `data` (0 <= childIdx < parent.children().size()).
    /// @throws IndexOutOfBoundsException if `childIdx` is out of bounds.
    Node<T> addChild(@NonNull Node<T> parent, @NonNull T data, int childIdx) throws IndexOutOfBoundsException;
    /// Removes `child` from `parent`.
    ///
    /// @return the removed subtree (with child as it's root)
    /// @throws NodeNotInTreeException if either `parent` or `child` are not contained in `this`.
    /// @throws IllegalArgumentException if `child` is not a child of `parent`.
    Tree<T> removeChild(@NonNull Node<T> parent, @NonNull Node<T> child) throws IllegalArgumentException;
    /// Two nodes are considered equal if they have the same data and position in the tree.
    ///
    /// This method requires that both `node` and `other` are contained in `this`.
    ///
    /// @return true if node and other are equal.
    /// @throws NodeNotInTreeException if either `node` or `other` are not contained in `this`.
    boolean equalNodes(Node<?> node, Node<?> other);


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
    /// # Example
    /// ```
    /// ├─ 1
    /// │  ├─ 2
    /// │  ├─ 8
    ///
    /// 1.idxOfChild(2) // returns 0
    /// 1.idxOfChild(8) // returns 1
    /// ```
    ///
    /// @return the index of `child` in its parent's children.
    /// @throws NodeNotInTreeException if `child` is not contained in `this`
    /// @throws IllegalArgumentException if `child` is not a child of `parent`.
    default int idxOfChild(@NonNull Node<T> parent, @NonNull Node<T> child) {
        if(!parent.hasChild(child))
            throw new IllegalArgumentException("Expected child to be a child of parent");

        return children(parent(child)).indexOf(child);
    }
    /// @return the number of children under `node`.
    /// @throws NodeNotInTreeException if node is not contained in this
    default int numChildren(@NonNull Node<T> node) {
        return node.children().size();
    }
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
    /// @throws NodeNotInTreeException if node is not contained in this
    default int depth(@NonNull Node<T> node) {
        int numParents = -1;
        Node<T> curr = node;

        while(curr != null) {
            numParents++;
            curr = parent(curr); // throws Exception if node not in `this`
        }

        return numParents;
    }
    /// @return true if `node` is the root node, aka `node` has no parent.
    /// @throws NodeNotInTreeException if `node` is not contained in `this`.
    default boolean isRoot(@NonNull Node<T> node) {return parent(node) == null;}
    /// @return true if `node` is a leaf node, aka `node` has no children.
    /// @throws NodeNotInTreeException if `node` is not contained in `this`.
    default boolean isLeaf(@NonNull Node<T> node) {return children(node).isEmpty();}

    /// Adds `data` as a child node to `parent`.
    /// @throws NodeNotInTreeException if `parent` is not contained in `this`.
    default Node<T> addChild(@NonNull Node<T> parent, @NonNull T data) {
        return addChild(parent, data, parent.childCount());
    }
    /// Adds `subtreeToAdd` to `this` below `parent` at index `childIdx`.
    ///
    /// It is acceptable for `subtreeToAdd` to be `this`. In this case, a duplicate of `this` will be created, and that will be added instead.
    /// @param parent The node to add the subtreeToAdd under
    /// @throws NodeNotInTreeException if `parent` is not contained in `this`.
    /// @throws IndexOutOfBoundsException if `childIdx` is out of bounds for parent. (0 <= childIdx < parent.childCount())
    /// @throws UnsupportedOperationException if `this` is immutable.
    default void addSubtree(@NonNull Node<T> parent, @NonNull Tree<T> subtreeToAdd, int childIdx) {
        if(subtreeToAdd == this) subtreeToAdd = subtreeToAdd.duplicate();

        Map<Node<T>, Node<T>> subtreeToThis = new HashMap<>();

        for(Node<T> node : subtreeToAdd) {
            var parentInThis = (node.isRoot())
                ? parent
                : subtreeToThis.get(node.parent());

            var nodeInThis = (node.isRoot())
                ? parentInThis.addChild(node.data(), childIdx)
                : parentInThis.addChild(node.data());

            subtreeToThis.put(node, nodeInThis);
        }
    }
    /// Adds `subtreeToAdd` to `this` below `parent`.
    ///
    /// It is acceptable for `subtreeToAdd` to be `this`. In this case, a duplicate of `this` will be created, and that will be added instead.
    /// @param parent The node to add the subtreeToAdd under
    /// @throws NodeNotInTreeException if `parent` is not contained in `this`.
    /// @throws UnsupportedOperationException if `this` is immutable.
    default void addSubtree(@NonNull Node<T> parent, @NonNull Tree<T> subtreeToAdd) {
        addSubtree(parent, subtreeToAdd, parent.childCount());
    }
    /// Removes `childToTrim` from `parent` and adds its children to its parent in same spot.
    ///
    /// If `childToTrim` is a leaf childToTrim, it is simply removed.
    ///
    /// NOTE: `childToTrim` cannot be the tree's root. In this case, use Tree.subtree() instead.
    ///
    /// # Example
    ///
    /// Before:
    /// ```
    /// ├─ 4
    /// │  ├─ 2
    /// │  │  ├─ 1
    /// │  │  ├─ 3
    /// │  ├─ 6
    /// │  │  ├─ 5
    /// │  │  ├─ 7
    /// ```
    /// After trim(2)
    /// ```
    /// ├─ 4
    /// │  ├─ 1
    /// │  ├─ 3
    /// │  ├─ 6
    /// │  │  ├─ 5
    /// │  │  ├─ 7
    /// ```
    ///
    /// @throws NodeNotInTreeException if `childToTrim` is not contained in `this`.
    /// @throws IllegalArgumentException if `childToTrim` is not a child of `parent`.
    default void trim(@NonNull Node<T> parent, @NonNull Node<T> childToTrim) throws IllegalArgumentException {
        if(!parent.hasChild(childToTrim))
            throw new IllegalArgumentException("Expected childToTrim to be a child of parent.");

        var nodeIdx = parent.idxOfChild(childToTrim);

        System.out.println(nodeIdx);

        var subtree = parent.removeChild(childToTrim);
        for(var child : subtree.root().children()) {
            parent.addSubtree(child.subtree(), nodeIdx + subtree.root().idxOfChild(child));
        }
    }

    /// Maps all `nodes` using the given mapper function
    /// @param <N> The output type
    /// @param mapper The mapping function
    /// @return the mapped tree
    default <N> Tree<N> map(Function<T, N> mapper) {
        Tree<N> newTree = ofRoot(mapper.apply(root().data()));

        BfsTreeIterator<T> iterator = new BfsTreeIterator<>(this);
        Map<Node<T>, Node<N>> thisToOther = new HashMap<>();
        thisToOther.put(root(), newTree.root());

        iterator.next();

        while (iterator.hasNext()) {
            var curr = iterator.next();

            var newNode = newTree.addChild(thisToOther.get(curr.parent()), mapper.apply(curr.data()));
            thisToOther.put(curr, newNode);
        }

        return newTree;
    }
    /// Note: it is not guaranteed that node ids will be preserved.
    ///
    /// @return a copy of `this` with only nodes under (and including) `root`.
    /// @throws NodeNotInTreeException if `root` is not contained in `this`.
    Tree<T> subtree(@NonNull Node<T> root);
    /// Duplicates the structure of `this`. Data in the duplicated tree, however, will point to the same objects as `this`.
    ///
    /// It is guaranteed that for any node in `this`, the matching node object in the return value will be different.
    /// In other words, for any matching pair of nodes (nodeInThis, nodeInResult), nodeInThis != nodeInResult.
    ///
    /// @return a clone of `this`.
    default Tree<T> duplicate() {
        return new AdjListTree<>(this);
    }
    /// @return an immutable version of `this`.
    default Tree<T> immutable() {
        return new ImmutableAdjListTree<>(this);
    }

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

            builder.append("│  ".repeat(Math.max(0, depth)));

            builder.append("├─ ").append(curr.data());
            if(iterator.hasNext()) builder.append("\n");
        }
        return builder.toString();
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
}
