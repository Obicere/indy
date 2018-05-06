# indy change log v0.003b

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

[v0.004b]: https://github.com/Obicere/indy/releases/tag/v0.004b
[v0.003b]: https://github.com/Obicere/indy/releases/tag/v0.003b
