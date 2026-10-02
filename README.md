# SBP Microservices

Spring Boot microservices monorepo: API gateway, auth, and user services with **dev** and **prod** profiles.

## Quick start

```bash
cp env/.env.dev.example env/.env.dev   # first time only
make db-dev                            # databases only
make start-dev                         # full stack in Docker
```

Local development with hot reload: `make db-dev`, then VS Code **Run and Debug → All services (dev)**.

See [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) for env variables, API paths, and VS Code setup.
