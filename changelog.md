# indy change log v0.004b

## [v0.005b] - Unreleased
### Added

- Statistics are now supported. 
They will print out at the end. 


    Execution completed in 5 seconds and 296 milliseconds
    Processed 4703 classes, 36776 methods
    Obscured 135559 method calls, 0 field calls
    Most prolific class was com/sun/tools/internal/ws/processor/modeler/wsdl/WSDLModeler with 1816 calls

- Code to _indy_ constructors has been added.
The code causes verification errors when ran, so it is currently disabled. 
Future Java versions may have patched the issue.

- Batches have been added. 
A variable number of files are processed per batch.
If the batch does not do any processing, a `db.info` file is not generated. 

- Specific error codes are going to be produced. 

- The fundamentals for localization. 
Existing messages will soon be replaced through the resource bundles. 

- Required files for the class files to execute are now exported into the output.

- Some new system arguments. Many (most) are non-functional and reserved. 
These will almost certainly change.

### Changed

- The `MethodFilter` has been properly renamed to `InstructionFilter`.
Changing the name makes the intent of the filtration clearer.
The original concept was to filter based on the invoked method.

- Renamed `Resolver$Resolution` to `Credential`. 

- In the credential class `Credential`, the interface flag was deemed worthless.
It has been repurposed as the array method flag. 
This is used to enforce resolution of methods called on arrays to the `Object` class.

- Attributes not required for the `Resolver` classes to be ran are stripped from the class files. 

- The name `info.db` is no longer used. 
Randomly generated file names with no extensions are used now. 

### Fixed

- Calling `Object#clone()` on array types resulted in a class casting exception.
This has been patched, but may restrict the Java version.
Currently only `1.8.0_112` and above is supported.

- Jar files within jar files results in the nested jar file being copied. 

### Deprecated

- `ClassFilter` - this was originally used to filter out the `Resolver` and `Credential` classes.
However, in theory these classes should be susceptible to replacement with an exception only being made when _indying_ indy. 

- `MethodFilter` - originally used to filter out constructors to avoid verification errors on super `<init>` calls.
Special code has been added to the `IndyMethodVisiter` to deal with this case. 
Note: this is the newly created method filter and not the renamed instruction filter.

- The existing `Log#error` methods have been deprecated. 
A new error system with specific codes is being rolled out. 

- The globbing for paths should no longer be used. 
This was due to an issue with how some command lines were treating the globbing. 

## [v0.004b] - 2018-05-04
### Added
- An `indy-min.jar` has been added to the build. 
This jar excludes the `asm-6.0.jar` libraries.
- `README.md` describing the project.
- `build/README.md` describing the builds.
- The `ClassFilter` interface, used to filter out classes from processing.
- Job operations, job batching will be done soon.
- Path utilities to perform basic operations: `org.obicere.indy.io.PathUtils`.
- Recursive directories and `.jar` files. 
Right now, the recursive depth limit does not work properly with `.jar` files. 

### Changed
- This changelog is now exported with the artifacts.
- The system has been largely changed from the `java.io` library to `java.nio`.

### Fixed
- An issue where the `info.db` file failed to load from `.jar` files. 

### Deprecated
- The static nature of the `info.db` file and `org.obicere.indy.exec.Resolver` has proven to be poor.
This was of course a temporary feature, and in the next patch this will be overhauled. 
Job batches will also be introduced soon. 
Since the jobs need to be done independent of each other, proper resolution is required.
Therefore, the lifeline of these classes will be deprecated and replaced. 

## [v0.003b] - 2018-05-03
### Added 
- Logging support.

## v0.002b - 2018-04-18
### Added
- Working model, minimal features.

## v0.001b - 2018-04-17
### Added
- First version release.
- If emojis aren't supported on your `.md` viewer, sorry in advance. :tada: :fireworks:

## 
### 

[v0.005b]: https://github.com/Obicere/indy/releases/tag/v0.005b
[v0.004b]: https://github.com/Obicere/indy/releases/tag/v0.004b
[v0.003b]: https://github.com/Obicere/indy/releases/tag/v0.003b
