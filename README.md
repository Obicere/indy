# indy

## About

**indy** is a Java obfuscation utility that utilizes the **in**voke **dy**namic instruction.

The intent of _indy_ is to obscure the resolution of members through static analysis.
The resolution of the name and type is performed at runtime. 
Once this is performed the Java virtual machine caches the callsite.
As a result, the performance impact of _indy_ occurs only at the first invocation of each member.

:coffee:

## Release

The releases can be found here: [releases]

## Execution

The execution of indy is rather straightforward:

````
Usage: indy <options> <files>
Where options include:
    -d: enables debug printing
    -s: enables stack printing when an error occurs
    -h: prints help
    -v: prints version information
````

The `options` are flags used to modify parameters of the execution.

The `files` are a list of paths to invoke _indy_ on. 
Currently the only paths supported are direct links to `.class` files. 
Support for `.jar` files, directories, and recursive directories is planned.

In the current version (v0.003b), the files are replaced. 
In the future, it is planned that an output directory can be specified. 

## Change Log

The entire changelog can be viewed here: [changelog]

## Support

The currently supported and tested Java versions:

- Java SE 7
- Java SE 8

Only standard Java libraries should be used. 
Certain libraries that rely on dynamic or runtime name resolution may be affected by this program. 
Certain Java virtual machines may have stricter conditions on the invoke dynamic verification process.
In these cases, _indy_ may work; however, there is no guarantee. 
The best way to find out if this obfuscator works is to try it. 

The following members are supported:

- [x] virtual methods
- [x] static methods
- [x] interface methods

The following members are not currently supported:

 - [ ] constructors
 - [ ] instance fields
 - [ ] static fields
 - [ ] invoke dynamic calls

## Dependencies

The following external libraries are required for this application to run:

 - [asm-6.0.jar]

Any necessary dependencies will be included in the [builds] folder. 

## More Information

 - [Java Virtual Machine Support for Non-Java Languages]

 - [invoke dynamic verification by type checking §4.10.1.9]
 
 - [invoke dynamic instruction specification §6.5]
 
 - [Java invoke application programming interface]

##

[releases]: https://github.com/Obicere/indy/releases 
[changelog]: https://github.com/Obicere/indy/blob/master/changelog.md
[asm-6.0.jar]: http://asm.ow2.io/
[builds]: https://github.com/Obicere/indy/tree/master/build
[Java Virtual Machine Support for Non-Java Languages]: https://docs.oracle.com/javase/8/docs/technotes/guides/vm/multiple-language-support.html
[invoke dynamic verification by type checking §4.10.1.9]: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.10.1.9.invokedynamic
[invoke dynamic instruction specification §6.5]: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.invokedynamic
[Java invoke application programming interface]: https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/package-summary.html
