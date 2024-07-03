# No4J
##  Simple logging library with sane defaults
![Example logs](/img/logs.png)
### Supports:

- Level hierarchy, including custom levels
- Console output separation between STDERR and STDOUT depending on severity 
- File output
- Rolling appender with file compression
- Basic inheritance

### Configuration
- Create `no4j.properties` file in the root directory of the project or in `src/main/resources`
- Every key starts with a `symbolic logger name`, recognized only during configuration and ends in a `key suffix` 
- Every `key` is followed by `=` followed by `value`
- The order of declarations or empty lines have no effect
- Inheritance occurs once all loggers are configured

### Configuration keys:
|         Key suffix |                Meaning                 |    Default value    |       Type        |
|-------------------:|:--------------------------------------:|:-------------------:|:-----------------:|
|            `.name` |             _logger name_              |                     |      String       |
|           `.level` |            _logging level_             |         OFF         |       Level       |
|    `.stderr.level` |         _STDERR logging level_         |        ERROR        |       Level       |
|        `.file.out` |            _log file path_             |        null         |       Path        |
|    `.file.enabled` |            _write to file_             |        false        |      boolean      |
| `.console.enabled` |           _write to console_           |        true         |      boolean      |
|    `.date.pattern` |         _date format pattern_          | yyyy-MM-dd HH:mm:ss | DateTimeFormatter |
|       `.date.zone` |           _UTC/GMT/UT zone_            |        UTC+0        |      ZoneId       |
|         `.inherit` | _symbolic logger name to inherit from_ |         N/A         |        N/A        |


### Example properties
```properties
main.package.name = org.some.package
main.package.level = ERROR
main.package.date.zone = UTC+6
main.package.file.out = main.package.log
main.package.file.enabled = true

mongodb.name = org.nosql.impl
mongodb.inherit = main.package
```

### Initialization - loading properties
```java
class Test {
    public static void main(String[] args) {
        try {
            PropertiesConfiguration.configure();
        } catch (IOException e) {
            /* Handle exception */
        }
    }
}
```