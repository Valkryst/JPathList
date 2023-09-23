`JPathList` is a Java Swing component designed to display a list of _unique_ directory and/or file paths. Additionally,
it supports drag-and-drop functionality, allowing users to drag files and directories from their file system into the
list.

## Table of Contents

* [Installation](https://github.com/Valkryst/JPathList#installation)
    * [Gradle](https://github.com/Valkryst/JPathList#-gradle)
    * [Maven](https://github.com/Valkryst/JPathList#-maven)
    * [sbt](https://github.com/Valkryst/JPathList#-scala-sbt)
* [Example](https://github.com/Valkryst/JPathList#example)
* [Supported Recursion Modes](https://github.com/Valkryst/JPathList/blob/master/src/main/java/com/valkryst/JPathList/RecursionMode.java)

## Installation

JPathList is hosted on the [JitPack package repository](https://jitpack.io/#Valkryst/JPathList)
which supports Gradle, Maven, and sbt.

### ![Gradle](https://i.imgur.com/qtc6bXq.png?1) Gradle

Add JitPack to your `build.gradle` at the end of repositories.

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

Add JPathList as a dependency.

```
dependencies {
	implementation 'com.github.Valkryst:JPathList:2023.9.21'
}
```

### ![Maven](https://i.imgur.com/2TZzobp.png?1) Maven

Add JitPack as a repository.

``` xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
Add JPathList as a dependency.

```xml
<dependency>
    <groupId>com.github.Valkryst</groupId>
    <artifactId>JPathList</artifactId>
    <version>2023.9.21</version>
</dependency>
```

### ![Scala SBT](https://i.imgur.com/Nqv3mVd.png?1) Scala SBT

Add JitPack as a resolver.

```
resolvers += "jitpack" at "https://jitpack.io"
```

Add JPathList as a dependency.

```
libraryDependencies += "com.github.Valkryst" % "JPathList" % "2023.9.21"
```

## Example

This creates a new `JPathList` and displays it in a `JFrame`. It is configured to display all files and directories
added to it, so you can immediately test the drag-and-drop functionality.

```java
public class Driver {
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            final var fileList = new com.valkryst.JPathList.JPathList();
            fileList.setRecursionMode(com.valkryst.JPathList.RecursionMode.FILES_AND_DIRECTORIES);

            final var frame = new JFrame("JPathList Example");
            frame.getContentPane().add(new JScrollPane(fileList));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setPreferredSize(new Dimension(500, 500));

            frame.setVisible(true);
            frame.pack();
            frame.setLocationRelativeTo(null);
        });
    }
}
```