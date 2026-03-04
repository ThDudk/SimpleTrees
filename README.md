# Java Simple Trees

## Description
Java tree library that supports java streams and Jackson Serialization, as well as some simple iterators.

I created this library because I was having trouble finding other libraries that met my needs: mapping nodes and cleanly serializing to JSON with Jackson.

## Compatibility

- Java 11+ (previous version untested)
- Jackson Databind 3.x.x

## Usage

By default, trees are mutable. You can create trees directly.

```java
// represents x + y = z
Tree<String> expressionTree = Tree.ofRoot("=");
Tree.Node<String> equals = expressionTree.root();
Tree.Node<String> add = equals.addChild("+");
add.addChild("x");
add.addChild("y");
equals.addChild("z");
```

Additionally, you can create the tree as follows:

```java
Tree<String> expressionTree = Tree.ofRoot("=").root() // focus on =
    .addChild("+") // focus on +
        .addChild("x").parent() // add x and focus on + again
        .addChild("y").parent() // add y and focus on + again
        .parent() // focus on =
    .addChild("z") // add z
    .tree(); // get the entire tree
```

### Iterators

The library comes with 2 simple iterators, `DfsTreeIterator` and `BfsTreeIterator`. 

By default, iterating through a tree or node will use `DfsIterator`.

```java
for(Tree.Node<String> node : expressionTree) {
    System.out.print(node.data() + ", ");
}
// outputs: =, +, x, y, z, 
```
You can also iterate through a subtree by providing a node as the input:
```java
for(Tree.Node<String> node : expressionTree.anyNodeWithData("+").orElseThrow()) {
    System.out.print(node.data() + ", ");
}
// outputs: +, x, y, 
```

### Using streams

Trees can be converted into streams then transformed back into a tree using `Tree.stream()`. 

This will create a stream of `StreamEdge`s, which are simplified data types meant to be manipulated in a stream.

If you wanted to map all nodes in the tree to another type, for example, you could do as follows:
```java
Tree<MathSymbol> exprTree = expressionTree.stream()
    .map(edge -> edge.mapNodes(nodeData -> MathSymbol.parseString(nodeData)))
    .collect(Tree.collector());
```

### Serialization with Jackson

Jackson serialization is already all setup. To serialize trees, simply call object mapper.

```java
// writes the tree as a json string
ObjectMapper mapper = new ObjectMapper();
String jsonString = mapper.writeValueAsString(expressionTree);

// reads the file in as a Tree
Tree<MathSymbol> exprTree = mapper.readValue(new File("src/main/resources/myExpressionTree.json"), new TypeReference<>(){});
```

for more information on jackson databind, see [github.com/FasterXML/jackson-databind](https://github.com/FasterXML/jackson-databind)