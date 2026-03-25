# Java Simple Trees

## Description
A Simple Java tree library that supports Jackson Serialization.

I created this library because I was having trouble finding other libraries that met my needs: mapping nodes, and cleanly serializing to JSON with Jackson.

> Note: This library is intended to be used with smaller trees (ideally < 1000 nodes).

## Compatibility

- Java 17+
- [Jackson Databind](https://github.com/FasterXML/jackson-databind) 3.0.0+

## Installation

Since the project is registered with Github Packages, you can simply pull from github in your pom.xml:

```xml
<repositories>
    <!-- ... -->
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/thdudk/simple-trees</url>
    </repository>
</repositories>

<dependencies>
    <!-- ... -->
    <dependency>
        <groupId>io.github.thdudk</groupId>
        <artifactId>simple-trees</artifactId>
        <version>2.0.0</version>
    </dependency>
</dependencies>
```

## Usage

### Creation

By default, trees are mutable; So, you can create trees directly using the Tree interface:

```java
// represents x + y = z
Tree<String> expressionTree = Tree.ofRoot("=");
Tree.Node<String> equals = expressionTree.root();
Tree.Node<String> add = equals.addChild("+");
add.addChild("x");
add.addChild("y");
equals.addChild("z");
```

Additionally, you can create trees as follows (though this can get confusing):

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
// # Iterating from a tree
for(Tree.Node<String> node : expressionTree) {
    System.out.print(node.data() + ", ");
}
// outputs: =, +, x, y, z, 

// # Iterating from a node
Node<String> additionNode = expressionTree.anyNodeWithData("+").orElseThrow();

for(Tree.Node<String> node : additionNode) {
    System.out.print(node.data() + ", ");
}
// outputs: +, x, y, 
```

### Mapping nodes

Mapping nodes is simply done using `Tree.map()`

```java
Tree<MathSymbol> exprTree = expressionTree
    .map(node -> MathSymbol.parseString(node));
```

### Serialization with Jackson

Jackson serialization is already all set up. To serialize trees, simply use Jackson's `ObjectMapper` class.

```java
// writes the tree as a JSON string
ObjectMapper mapper = new ObjectMapper();
String jsonString = mapper.writeValueAsString(expressionTree);

// reads the file in as a Tree
Tree<MathSymbol> exprTree = mapper.readValue(new File("src/main/resources/myExpressionTree.json"), new TypeReference<>(){});
```

> for more information on Jackson databind, see [github.com/FasterXML/jackson-databind](https://github.com/FasterXML/jackson-databind)

### Immutable Trees

You can make a tree immutable using Tree.immutable(). 

```java
Tree<String> myTree = ...;
Tree<String> immutableTree = myTree.immutable();

immutableTree.addChild(immutableTree.root(), "newChild"); // throws Unsupported Operation Exception
immutableTree.removeChild(immutableTree.root(), "firstChild"); // throws Unsupported Operation Exception
```