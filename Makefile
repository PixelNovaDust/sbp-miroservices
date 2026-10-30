.DEFAULT_GOAL := help

.PHONY: help install-lib migrate-dev migrate-prod infra-dev start-dev infra-prod start-prod down-dev down-dev-volumes reset-dev down-prod

ENV_DEV := env/.env.dev
ENV_PROD := env/.env.prod
COMPOSE_DEV := docker compose -p sbp-dev --env-file $(ENV_DEV) -f docker/docker-compose.dev.yaml
COMPOSE_PROD := docker compose -p sbp-prod --env-file $(ENV_PROD) -f docker/docker-compose.prod.yaml

# Infrastructure only (DBs, Prometheus, Grafana) — start Eureka + microservices from the IDE
INFRA_DEV_SERVICES := auth-db user-db person-db studio-db content-db reference-db prometheus grafana
INFRA_PROD_SERVICES := auth-db user-db person-db studio-db content-db reference-db prometheus grafana

help:
	@echo SBP Microservices — make targets
	@echo.
	@echo   make help         Show this help
	@echo   make install-lib  Install sbp-lib-common into local ~/.m2 (fixes IDE errors)
	@echo   make migrate-dev  Run Liquibase on dev DBs (all Postgres containers must be up)
	@echo   make migrate-prod Run Liquibase on prod DBs
	@echo   make infra-dev    Dev infra + automatic migrations (stack sbp-dev)
	@echo   make start-dev    Full dev stack: DBs, migrations, then all services
	@echo   make down-dev     Stop sbp-dev stack (keeps DB volumes)
	@echo   make down-dev-volumes  Stop sbp-dev and delete DB volumes
	@echo   make reset-dev    down-dev-volumes + infra-dev (after DB password change)
	@echo   make infra-prod   Prod infra + automatic migrations (stack sbp-prod)
	@echo   make start-prod   Full prod stack: DBs, migrations, then all services
	@echo   make down-prod    Stop sbp-prod stack

install-lib:
	./mvnw -f sbp-lib-common/pom.xml clean install -DskipTests

migrate-dev:
	$(COMPOSE_DEV) run --rm --build sbp-migration

migrate-prod:
	$(COMPOSE_PROD) run --rm --build sbp-migration

infra-dev:
	$(COMPOSE_DEV) up -d $(INFRA_DEV_SERVICES)
	$(MAKE) migrate-dev

start-dev:
	$(COMPOSE_DEV) up -d auth-db user-db person-db studio-db content-db reference-db
	$(MAKE) migrate-dev
	$(COMPOSE_DEV) up -d --build

infra-prod:
	$(COMPOSE_PROD) up -d $(INFRA_PROD_SERVICES)
	$(MAKE) migrate-prod

start-prod:
	$(COMPOSE_PROD) up -d auth-db user-db person-db studio-db content-db reference-db
	$(MAKE) migrate-prod
	$(COMPOSE_PROD) up -d --build

down-dev:
	$(COMPOSE_DEV) down

down-dev-volumes:
	$(COMPOSE_DEV) down -v

reset-dev:
	$(COMPOSE_DEV) down -v
	$(MAKE) infra-dev

down-prod:
	$(COMPOSE_PROD) down
