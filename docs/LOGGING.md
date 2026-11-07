# Logging (`com.sbp.common.logging`)

Shared logging lives in **`sbp-lib-common`**. Services that depend on lib-common get rolling category log files, optional Tomcat access logs, and monthly archives when enabled.

Implementation overview: [LIB-COMMON.md](LIB-COMMON.md) (logging module).

## Categories

Inject **`SbpLogger`** and use the category that matches the concern:

| Method             | Logger name                      | Typical file            |
| ------------------ | -------------------------------- | ----------------------- |
| `application()`    | `com.sbp.logging.application`    | `ApplicationLog.log`    |
| `security()`       | `com.sbp.logging.security`       | `SecurityLog.log`       |
| `authentication()` | `com.sbp.logging.authentication` | `AuthenticationLog.log` |
| `database()`       | `com.sbp.logging.database`       | `DatabaseLog.log`       |

Errors from category appenders are also copied to **`ErrorLog.log`**.

```java
import com.sbp.common.logging.SbpLogger;

@Service
@RequiredArgsConstructor
public class ExampleService {
    private final SbpLogger log;

    void run() {
        log.application().info("Processing request");
        log.database().debug("Query finished");
    }
}
```

When **`sbp.logging.bridge-application-package=true`** (default), loggers under **`com.sbp.*`** (configurable via **`sbp.logging.application-package`**) are bridged into the application category file.

## Configuration (`sbp.logging.*`)

| Property               | Default                | Meaning                                        |
| ---------------------- | ---------------------- | ---------------------------------------------- |
| `enabled`              | `true`                 | Master switch for SBP logback wiring           |
| `directory`            | `logs`                 | Active log directory (per service working dir) |
| `archive-directory`    | `logs/archive`         | Monthly tarball destination                    |
| `daily-rollover`       | `true`                 | Date-stamped rolled files                      |
| `monthly-archive`      | `true`                 | Run archive job on schedule                    |
| `monthly-archive-cron` | `0 5 0 1 * ?`          | Archive cron (1st of month, 00:05)             |
| `access-log-enabled`   | `true`                 | Tomcat **`localhost_access_log`**              |
| `access-log-file-name` | `localhost_access_log` | Access log base name                           |
| `catalina-out-enabled` | `true`                 | Capture catalina output where applicable       |
| `level.application`    | `INFO`                 | Application category level                     |
| `level.security`       | `INFO`                 | Security category level                        |
| `level.authentication` | `INFO`                 | Authentication category level                  |
| `level.database`       | `INFO`                 | Database category level                        |
| `level.root`           | `INFO`                 | Root level when SBP config is active           |

Set levels in `application-dev.properties` / env as needed, for example:

```properties
sbp.logging.level.application=DEBUG
sbp.logging.level.database=WARN
```

## Archives

**`LogArchiveService`** compresses configured base names (application, security, authentication, database, error, catalina, access log) into **`logs/archive/`**. **`LogArchiveScheduler`** runs on **`monthly-archive-cron`** when **`monthly-archive=true`**.

## Auto-configuration

- **`SbpLoggingAutoConfiguration`** — logback + **`SbpLogger`** bean when `sbp.logging.enabled=true`.
- **`TomcatAccessLogCustomizer`** — access log path under `sbp.logging.directory`.
- Registered via **`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`** in lib-common.

Disable SBP logging entirely: `sbp.logging.enabled=false` (service falls back to normal Spring Boot logging only).
