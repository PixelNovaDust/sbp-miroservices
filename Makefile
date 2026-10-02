.PHONY: db-dev start-dev db-prod start-prod down-dev down-prod

ENV_DEV := env/.env.dev
ENV_PROD := env/.env.prod
COMPOSE_DEV := docker compose --env-file $(ENV_DEV) -f docker/docker-compose.dev.yaml
COMPOSE_PROD := docker compose --env-file $(ENV_PROD) -f docker/docker-compose.prod.yaml

db-dev:
	$(COMPOSE_DEV) up -d auth-db user-db

start-dev:
	$(COMPOSE_DEV) up -d --build

db-prod:
	$(COMPOSE_PROD) up -d auth-db user-db

start-prod:
	$(COMPOSE_PROD) up -d --build

down-dev:
	$(COMPOSE_DEV) down

down-prod:
	$(COMPOSE_PROD) down
