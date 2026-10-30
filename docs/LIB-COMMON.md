# sbp-lib-common

One Maven artifact for all shared microservice code: **`com.sbp:sbp-lib-common:1.0.0`**.

Java layout under **`com.sbp.common`** — types are split by **module subpackage** (folders), not scattered in one flat package.

## Module layout

```
com.sbp.common/
├── package-info.java
├── response/          # API JSON envelope
│   ApiResponse, ApiError, ApiResponseFactory, PageMetadata, ApiResponseCode
├── client/            # RestClient helpers (requires spring-web on the service classpath)
│   ApiClientSupport
└── logging/           # SLF4J categories, log files, archive
    SbpLogger, SbpLoggingProperties, …
```

## Install

```bash
cd sbp-lib-common
mvn clean install
```

## Dependency

```xml
<dependency>
  <groupId>com.sbp</groupId>
  <artifactId>sbp-lib-common</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Imports

**Response module**

```java
import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;
```

**Logging module**

```java
import com.sbp.common.logging.SbpLogger;
```

New shared areas get a new subpackage under `com.sbp.common.<module>` (for example `com.sbp.common.security` later), not a new Maven module, unless lifecycle requires it.
