# No4J
##  Simple logging library with sane defaults
![Example logs](https://github.com/user-attachments/assets/404fba52-c025-461b-aefc-098c1c2709d5)
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
|              Key suffix |                Meaning                 |    Default value    |       Type        |
|------------------------:|:--------------------------------------:|:-------------------:|:-----------------:|
|                 `.name` |             _logger name_              |                     |      String       |
|                `.level` |            _logging level_             |         OFF         |       Level       |
|         `.stderr.level` |         _STDERR logging level_         |        ERROR        |       Level       |
|             `.file.out` |            _log file path_             |        null         |       Path        |
|    `.file.rolling.size` |  _file size to reach before rolling_   |       4194304       |       Long        |
|         `.file.enabled` |            _write to file_             |        false        |      boolean      |
| `.file.rolling.enabled` |            _roll log files_            |        false        |      boolean      |
|      `.console.enabled` |           _write to console_           |        true         |      boolean      |
|         `.date.pattern` |         _date format pattern_          | yyyy-MM-dd HH:mm:ss | DateTimeFormatter |
|            `.date.zone` |           _UTC/GMT/UT zone_            |        UTC+0        |      ZoneId       |
|              `.inherit` | _symbolic logger name to inherit from_ |         N/A         |        N/A        |


### Example properties
```properties
main.package.name = org.some.package
main.package.level = ERROR
main.package.date.zone = UTC+6
main.package.file.out = main.package.log
main.package.file.enabled = true
main.package.file.rolling.size = 1048576
main.package.file.rolling.enabled = false

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

### Configuring programmatically

```java
import no4j.core.Level;
import no4j.core.Logger;

class Test {
    public static void main(String[] args) {
        Logger log = Logger.getLogger("main.package");
        log.setLoggingLevel(Level.ERROR);
        log.setOutput("main.package.log");
        log.getConfig().enableFileOutput(true);
        log.getAppender().setRollSize(2 * 1024 * 1024);
        log.getAppender().setRolling(true);
        
        // USAGE
        log.debug("DEBUG");
        log.info("INFO");
        log.warn("WARNING");
        log.fatal("FATAL");
        log.unreachable("UNREACHABLE");
        log.exception(new RuntimeException("Exception thrown!"));
    }
}
```

### ANSI color support
**Generally supported within all Linux terminals**

Windows `cmd` or `powershell` generally do not support ANSI.
It is possible to enable ANSI in Windows 10 and later by creating a DWORD(32-bit) `VirtualTerminalLevel` with value `1` at `HKEY_CURRENT_USER\Console` in registry
<br>

**Alternative terminals for Windows that support ANSI**
<ul>
  <li>Git Bash</li>
  <li>Cmder</li>
  <li>IntelliJ console</li>
</ul>



