# No4J

## Simple logging library with sane defaults

![Example logs](https://github.com/user-attachments/assets/404fba52-c025-461b-aefc-098c1c2709d5)
![Colored logs](https://github.com/user-attachments/assets/2e118b8c-e451-4053-93a9-83a654beda45)

### Supports:

- Level hierarchy, including custom levels
- Console output separation between STDERR and STDOUT depending on severity
- File output
- Rolling appender with file compression
- Basic inheritance
- ANSI color printing
- Logging per site as an extension

### Configuration and properties
The configuration file follows **INI** format:
```ini
[symbolic-name]
name = org.some.package
level = ERROR
msg.length = 10000
date.zone = UTC+6
file.out = main.package.log
file.enabled = true
file.rolling.size=1048576
file.rolling.enabled=false

[mongodb]
name = org.nosql.impl
inherit = main.package
```

Each logger declaration has its own symbolic name enclosed in square brackets.<br>
Every `key` is followed by `=` followed by `value`. Two key-value pairs are not allowed on the same line. <br>
Values are not enclosed in quotation marks. Comments are allowed but must be preceded with a `#`. <br>
The order of declarations does not matter. Inheritance occurs once all loggers are configured, and it is performed in no particular order.
The config file `no4j.ini` must be in the root directory of the project or in `src/main/resources`.
Properties such as file output path are excluded from inheritance to prevent collisions.

### Configuration keys:

|              Key suffix |                Meaning                 |     Default value      |       Type        |
|------------------------:|:--------------------------------------:|:----------------------:|:-----------------:|
|                  `name` |             _logger name_              |                        |      String       |
|                 `level` |            _logging level_             |          OFF           |       Level       |
|            `msg.length` |     _trimming length for messages_     | Integer.MAX_Value - 32 |        int        |
|          `stderr.level` |         _STDERR logging level_         |         ERROR          |       Level       |
|              `file.out` |            _log file path_             |          null          |       Path        |
|     `file.rolling.size` |  _file size to reach before rolling_   |        4194304         |       Long        |
|          `file.enabled` |            _write to file_             |         false          |      boolean      |
|  `file.rolling.enabled` |            _roll log files_            |         false          |      boolean      |
|       `console.enabled` |           _write to console_           |          true          |      boolean      |
|          `date.pattern` |         _date format pattern_          |  yyyy-MM-dd HH:mm:ss   | DateTimeFormatter |
|             `date.zone` |           _UTC/GMT/UT zone_            |         UTC+0          |      ZoneId       |
|               `inherit` | _symbolic logger name to inherit from_ |          N/A           |        N/A        |


### Initializing by loading the configuration file

```java
import no4j.core.No4JConfiguration;

class Test {
    public static void main(String[] args) {
        try {
            No4JConfiguration.configure();
        } catch (IOException e) {
            /* Handle exception */
        }
    }
}
```

### Configuring programmatically

```java
import no4j.core.FileAppender;
import no4j.core.Level;
import no4j.core.Logger;
import no4j.extensions.LoggerBuilder;

class Test {
    public static void main(String[] args) {
        Logger log = LoggerBuilder.error("main.package")
                .file("main.package.log")
                .rollAtMegabytes(2)
                .maxMessageLength(5000)
                .getLogger();

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
It is possible to enable ANSI in Windows 10 and later by creating a DWORD(32-bit) `VirtualTerminalLevel` with value `1`
at `HKEY_CURRENT_USER\Console` in registry
<br>

**Alternative terminals for Windows that support ANSI**
<ul>
  <li>Git Bash</li>
  <li>Cmder</li>
  <li>IntelliJ console</li>
</ul>

## Jitpack import

### Maven

```xml

<dependency>
    <groupId>com.github.FriskIsGit</groupId>
    <artifactId>no4j-logger</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```

By default, all POMs implicitly inherit from a super-POM:
https://maven.apache.org/ref/3.0.4/maven-model-builder/super-pom.html
When an external repository is specified the default repository needs to be respecified:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>

    <repository>
        <id>central</id>
        <name>Central Repository</name>
        <url>https://maven.org/maven2</url>
    </repository>
</repositories>
```

### Gradle

Include at root level in build.gradle at the end of repositories:

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency:

```groovy
dependencies {
    implementation 'com.github.FriskIsGit:no4j-logger:main-SNAPSHOT'
}
```





