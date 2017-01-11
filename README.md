# JarShrink
Shrinks .jar files by removing redundant class-files.

## Motivation

Looking for a way to statically link libraries in Java as you would do in C/C++, I was surprised to find no proper way. <br>
So the next best thing that came to mind was to look for tools that would at least remove unused classes from a jar. Amazed by how slowly ProGuard does it's job, I wrote this clean simple tool.

## Commandline Interface

### Grammar

    jarShrink <jarFile> [<argumentName> <argumentValue>]
     
### Arguments

    -o | -out       Specifies the output-file for the newly created jar.
    
    -k | -keep      Specifies a package or class that will be retained together with it's dependencies. 
                    Can be called multiple times.
                    
    -s | -status    Print status information while processing.
    
    -n | -nolist    Don't print a list of the remaining dependencies.
    
### Example

    jarShrink "my.jar" -out "my_shrunken.jar" -status -keep "some.package.with.reflection"
    
## API

JarShrink's functionality is encapsulated in the class <b>JarShrinker</b> for API use.

### Examples

A minimal example would be:

    JarShrinker shrinker = new JarShrinker();
    shrinker.shrink(jarFile, outFile);

A more complete example:

    JarShrinker shrinker = new JarShrinker(tmpDir);
    shrinker.setPrintStatus(true);
    shrinker.setPrintDependencyList(true);
    
    shrinker.shrink(jarFile, outFile, keeps);
   

## Prequisites

 - Java 8 or higher
 - JDK to run from commandline or any implementation of jdeps for API use
 
## A few notes

 - JarShrink won't touch included .jar files. This is not due to lazyness but to retain the ability to make sure that libraries, that make use of reflection will still keep their full functionality.
