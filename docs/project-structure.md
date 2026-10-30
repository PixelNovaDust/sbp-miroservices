# Project structure

```
sbp-microservices/
├── pom.xml                    # Maven reactor (build lib-common + services)
├── docker/                    # docker-compose.dev.yaml, docker-compose.prod.yaml
├── docs/                      # Documentation (CAPS filenames)
├── env/                       # Profile env files (.env.dev, .env.prod) + *.example templates
├── sbp-lib-common/            # Shared library (API response, logging, common utilities)
├── sbp-migration/             # Liquibase changelogs + migration runner (auth + user DBs)
├── static/email.html          # Plain HTML email form (no backend)
├── sbp-discovery-server/      # Eureka registry
├── sbp-admin-service/         # Monitoring APIs
├── sbp-api-gateway/
├── sbp-auth-services/
├── sbp-user-services/
├── sbp-person-service/
├── sbp-studio-service/
├── sbp-content-service/
├── docker/prometheus/         # Prometheus scrape config (static targets)
├── docker/grafana/            # Grafana provisioning
├── Makefile
├── README.md
├── .gitignore
├── .gitattributes
└── .vscode/                   # Shared IDE config (launch, tasks, settings)
```

Each Spring Boot service uses the layer packages under `com.sbp.<service>` documented in [SERVICE-PACKAGE-LAYOUT.md](SERVICE-PACKAGE-LAYOUT.md) (`config`, `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `exception`, `security`, `util`).

Each service module also includes:

- `application.properties` — shared settings and context paths
- `application-dev.properties` — dev datasource, DevTools
- `application-prod.properties` — prod datasource, stricter JPA
- `META-INF/additional-spring-configuration-metadata.json` — IDE property hints
- `Dockerfile` — multi-stage image build (uses Maven from the build image; no per-module wrapper)
