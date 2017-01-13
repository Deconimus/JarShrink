# JarShrink
Shrinks JARs by removing redundant class-files.

## Motivation

Looking for a way to statically link libraries in Java as you would do in C/C++, I was surprised to find no proper way. <br>
So the next best thing that came to mind was to look for tools that would at least remove unused classes from a jar. Amazed by how slowly ProGuard does it's job, I wrote this clean simple tool.

## How to use

You can either run JarShrink as a commandline tool or integrate it into your software by importing it and calling it's API.<br>
Instructions on how to use these interfaces are found in the sections further below.

If your software or software you're including makes use of Reflection, you will most likely need to tell JarShrink to keep certain packages or whole libaries in order to preserve their functionality. I've started compiling a table of known libaries with information regarding their compatibility with JarShrink further below this readme.

There are pre-built JARs in the [release-section](https://github.com/Deconimus/JarShrink/releases), so you won't necessarily have to build JarShrink yourself.<br>
However there is also a python build-script.

## Prequisites

 - Java 8 or higher
 - JDK to run from commandline or any implementation of jdeps for API use

## Commandline Interface

### Grammar

    jarShrink <jarFile> [<argumentName> <argumentValue>]
     
### Arguments

Argument          | Value | Effect
----------------- | ----- | ------
`-o` or `-out`    | directory | Specifies the output-file for the newly created jar.
`-k` or `-keep`   | package or class | Specifies a package or class that will be retained together with it's dependencies.<br>Can be called multiple times.
`-s` or `-status` | | Print status information while processing.
`-n` or `-nolist` | | Don't print a list of the remaining dependencies.
    
### Example

    jarShrink "my.jar" -out "my_shrunken.jar" -status -keep "some.package.with.reflection"
    
## API

JarShrink's functionality is encapsulated in the class `JarShrinker` for API use.

### Examples

A minimal example would be:

    JarShrinker shrinker = new JarShrinker();
    shrinker.shrink(jarFile, outFile);

A more complete example:

    JarShrinker shrinker = new JarShrinker(tmpDir);
    shrinker.setPrintStatus(true);
    shrinker.setPrintDependencyList(true);
    
    shrinker.shrink(jarFile, outFile, keeps);
   

## How it works

A basic summary of JarShrink's procedure:

 - Extract the jar's contents into a temporary directory.
 - Use `jdeps` to generate a dependency-map of all classes inside the jar.
 - Search for a Main-Class specified in the MANIFEST.MF file.
 - Construct a Dependency-Tree with the Main-Class and/or the specified classes/packages to keep as it's root.
 - Remove all class-files from the temporary directory that aren't in Dependency-Tree and scrap folders that are now empty.
 - Build a new jar from the remaining contents of the temporary directory.
 
## Compatibility with known libraries

Below is a table of known libraries and how well JarShrink does with them.<br>
_Note that any library will still work if imported as a jar file._

Library | Compatibility | Extra arguments (if needed)
--------|:-------------:|----------------------------
LWJGL | ✓ | 
JInput | (✓) | `-keep net.java.games.input`
Guava | ✓ | 
Dom4j | ✓ | 
Slick2D | ✓ | 
jbzip2 | ✓ | 
HTMLUnit | X | 


## A few notes

 - JarShrink won't touch included .jar files. This is not due to lazyness but to retain the ability to make sure that libraries, that make use of reflection will still keep their full functionality.
