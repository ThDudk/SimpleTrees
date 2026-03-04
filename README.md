# Java Simple Trees

## Description
A Java tree library that supports Java streams and Jackson Serialization.

I created this library because I was having trouble finding other libraries that met my needs: mapping nodes, and cleanly serializing to JSON with Jackson.

## Compatibility

- Java 11+ (previous versions untested)
- [Jackson Databind](https://github.com/FasterXML/jackson-databind) v3+

## Installation

To install the project, first go and download the [latest release]().

### Option 1: Using a libs directory

To generate a libs directory, open your terminal and cd to your project directory 
> If you're using IntelliJ, you can use the built-in `View/Tool Windows/Terminal`. Just make sure the simple-trees jar is inside your project directory.

From there, use the following command to create a project level Maven repository: (replacing the version where necessary)

```terminaloutput
mvn install:install-file 
    -Dfile=simple-trees-v1.0.0.jar
    -DgroupId=io.github.thdudk
    -DartifactId=simple-trees
    -Dversion=1.0.0
    -Dpackaging=jar
    -DlocalRepositoryPath=libs
    -DcreateChecksum=true
```

Then register the Maven repository and add simple-trees as a dependency.

```xml
<repositories>
    <repository>
        <id>in-project</id>
        <name>In Project Repo</name>
        <url>file://${project.basedir}/libs</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.thdudk</groupId>
        <artifactId>simple-trees</artifactId>
        <!-- replace with your version -->
        <version>1.0.0</version> 
    </dependency>
</dependencies>
```

> **NOTE:** This option ensures that anyone cloning your repository will also have access to simple-trees.

### Option 2: Registering with the Local Maven Repository

To register to your device's local maven repository, first ensure you have Maven installed

If you don't have Maven installed, you can download it using `brew install maven`, or directly from [maven.apache.org/download.cgi](https://maven.apache.org/download.cgi).

Once Maven is installed, you can run the following command to add simple-trees to your local repository.

```terminaloutput
mvn install:install-file 
    -Dfile=simple-trees-v1.0.0.jar
    -DgroupId=io.github.thdudk
    -DartifactId=simple-trees
    -Dversion=1.0.0
    -Dpackaging=jar
```

Then, you can add simple-trees like any other dependency in your pom.xml

```xml
<dependency>
    <groupId>io.github.thdudk</groupId>
    <artifactId>simple-trees</artifactId>
    <version>1.0.0</version>
</dependency>
```

> **Note:** Option 2 requires that every device using this project MUST have the simple-trees jar registered, which can be unrealistic. 
> 
> Option 1 is preferred in most cases. 

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

### Using streams

Trees can be converted into streams, then transformed back into a tree using `Tree.stream()`. 

This will create a stream of `StreamEdges', which are simplified data types meant to be manipulated in a stream.

If you wanted to map all nodes in the tree to another type, for example, here's how you would do it:
```java
Tree<MathSymbol> exprTree = expressionTree.stream()
    .map(edge -> edge.mapNodes(nodeData -> MathSymbol.parseString(nodeData)))
    .collect(Tree.collector());
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

> for more information on jackson databind, see [github.com/FasterXML/jackson-databind](https://github.com/FasterXML/jackson-databind)