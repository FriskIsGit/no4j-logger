# No4J
##  Simple logging library with sane defaults
![Example logs](/img/logs.png)
### Features:

- Level hierarchy, including custom levels
- All loggers are OFF by default
- Anonymous loggers, named loggers and a global logger
- Output is directed to STDERR instead of STDOUT depending on severity
- Output can be directed to files
- Basic inheritance from another logger

### Configuration
1. Create `no4j.properties` file in the root directory of the project or in `src/main/resources`
2. Every key is followed by an equal sign and a value
3. Every key starts with a symbolic logger name, recognized only during configuration
4. The order of declarations or empty lines have no effect.

### Configuration keys:
| Key suffix         | Underlying type |               Meaning                |
|:-------------------|:---------------:|:------------------------------------:|
| `.name`            |     String      |             logger name              |
| `.inherit`         |    _string_     | symbolic logger name to inherit from |
| `.level`           |     _Level_     |         STDOUT logging level         |
| `.stderr.level`    |     _Level_     |         STDERR logging level         |
| `.file.out`        |     _Path_      |            log file path             |
| `.file.enabled`    |    _boolean_    |            write to file             |
| `.console.enabled` |    _boolean_    |           write to console           |


### Example properties
```properties
main.package.name = org.some.package
main.package.level = ERROR
main.package.file.out = main_package.log
main.package.file.enabled = true

mongodb.name = org.nosql.impl
mongodb.inherit = main.package
```