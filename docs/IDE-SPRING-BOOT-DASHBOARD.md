# Spring Boot Dashboard (VS Code / Cursor)

The **Spring Boot Dashboard** lists runnable apps that the **Java extension** has imported from Maven. It does **not** read Docker Compose or `launch.json` alone.

## Checklist when a module is missing (e.g. `sbp-reference-service`)

1. **Folder exists**  
   Confirm `sbp-reference-service/pom.xml` and `SbpReferenceServiceApplication.java` are on disk.

2. **Root reactor**  
   Open the repo **root** `pom.xml` (not only a subfolder). The root must list:

   ```xml
   <module>sbp-reference-service</module>
   ```

3. **Java Projects view**  
   View → **Java Projects** (or Explorer → **JAVA PROJECTS**).  
   You should see **`sbp-reference-service`** under the reactor.
   - If it is **missing** → Maven import did not pick up the module (go to step 5).
   - If it is **red / has errors** → fix classpath (usually step 4), then refresh.

4. **Install shared library**  
   Run task **Maven: install sbp-lib-common** or:

   ```powershell
   .\scripts\run.ps1 install-lib
   ```

   Then task **Maven: compile sbp-reference-service**.

5. **Force Maven reimport**
   - Command Palette → **Java: Clean Java Language Server Workspace** → Reload window.
   - After reload, wait until **Java Projects** finishes loading (status bar).
   - Command Palette → **Java: Import Java projects in workspace** (if available).
   - Or right-click root `pom.xml` → **Update Project Configuration**.

6. **Spring Boot Dashboard refresh**
   - Open the **Spring Boot Dashboard** view once (activates the extension).
   - Use the **Refresh** icon on the dashboard.
   - If refresh fails with “command not found”, reload the window or re-enable **Spring Boot Dashboard** extension.

7. **Run without dashboard**  
   Run and Debug → **Reference (dev)** or compound **Domain services (dev)**.  
   Direct run:
   ```powershell
   .\mvnw.cmd -f sbp-reference-service/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   (Set `REFERENCE_DB_*` / use `env/.env.dev` in IDE launch.)

## Expected apps in this repo

| Maven module              | Main class                                             | Port (dev) |
| ------------------------- | ------------------------------------------------------ | ---------- |
| sbp-discovery-server      | `com.sbp.discovery.SbpDiscoveryServerApplication`      | 8761       |
| sbp-api-gateway           | `com.sbp.gateway.SbpApiGatewayApplication`             | 8080       |
| sbp-auth-services         | `com.sbp.auth.SbpAuthServicesApplication`              | 8081       |
| sbp-user-services         | `com.sbp.user.SbpUserServicesApplication`              | 8082       |
| sbp-admin-service         | `com.sbp.admin.SbpAdminServiceApplication`             | 8083       |
| sbp-person-service        | `com.sbp.person.SbpPersonServiceApplication`           | 8084       |
| sbp-studio-service        | `com.sbp.studio.SbpStudioServiceApplication`           | 8085       |
| sbp-content-service       | `com.sbp.content.SbpContentServiceApplication`         | 8086       |
| **sbp-reference-service** | **`com.sbp.reference.SbpReferenceServiceApplication`** | **8087**   |

## Root `.project` (DBeaver)

The repo root `.project` is for **DBeaver** only (no Java builder). Java import uses **Maven** via root `pom.xml`; that is normal and should not block the reference module.

## Still stuck?

In **Output** panel, select **Language Support for Java** and look for import errors mentioning `sbp-reference-service`. In **Problems**, filter by project name.
