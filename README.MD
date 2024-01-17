# No4J
##  Simple logging library with sane defaults
### Features:

- All loggers are OFF by default
- There are anonymous loggers, named loggers and a global logger
- Output is directed to STDERR instead of STDOUT depending on severity
- Output can be directed to files
- Basic inheritance from another logger

### Configuration
1. Create `no4j.properties` file in the root directory of the project or in `src/main/resources`
2. Every key is followed by an equal sign and a value
3. Every key starts with a symbolic logger name, recognized only during configuration
4. The order of declarations or empty lines have no effect.

### Available configuration keys:

> `.name` = String <br>
> `.level` = Level <br>
> `.stderr.level` = Level <br>
> `.file.out` = File <br>
> `.file.enabled` = boolean <br>
> `.console.enabled` = boolean <br>
> `.inherit` = String <br>

### Example properties
```properties
main.package.name = org.some.package
main.package.level = ERROR
main.package.file.out = main_package.log
main.package.file.enabled = true

mongodb.name = org.nosql.impl
mongodb.inherit = main.package
```