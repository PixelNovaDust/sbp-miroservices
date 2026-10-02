# Project structure

```
sbp-microservices/
├── docker/                    # docker-compose.dev.yaml, docker-compose.prod.yaml
├── docs/                      # Documentation (CAPS filenames)
├── env/                       # Profile env files (.env.dev, .env.prod) + *.example templates
├── sbp-api-gateway/
├── sbp-auth-services/
├── sbp-user-services/
├── Makefile
├── README.md
├── .gitignore
├── .gitattributes
└── .vscode/                   # Shared IDE config (launch, tasks, settings)
```

Each service is a standalone Spring Boot Maven module with:

- `application.properties` — shared settings and context paths
- `application-dev.properties` — dev datasource, DevTools
- `application-prod.properties` — prod datasource, stricter JPA
- `META-INF/additional-spring-configuration-metadata.json` — IDE property hints
- `Dockerfile` — multi-stage image build (uses Maven from the build image; no per-module wrapper)
