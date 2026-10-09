# IntelliSure Docker development

Docker support is an additive local-development option. The existing Windows startup scripts and native configuration remain unchanged and are still the authoritative office-laptop workflow.

## Start the backend stack

From the repository root:

```sh
export MYSQL_ROOT_PASSWORD='use-a-local-only-password'
export INTELLISURE_JWT_SECRET="$(openssl rand -base64 32 | tr -d '\\n')"
docker compose up --build
```

`INTELLISURE_JWT_SECRET` must be a Base64-encoded value because the services decode it when creating their JWT signing key. Keep both values local-only and never commit them.

The stack includes MySQL, Eureka, API Gateway, and all nine backend services. The Gateway is available at `http://localhost:8080`, Eureka at `http://localhost:8761`, and the service ports remain 8081–8089 as configured by the native applications.

The MySQL data is persisted in the `intellisure-mysql-data` Docker volume. The initialization script creates only the application databases; it does not insert business records.

## Common operations

```sh
# Start in the background
docker compose up --build -d

# View service logs
docker compose logs -f api-gateway

# Check container health
docker compose ps

# Stop containers and preserve local database data
docker compose down

# Rebuild one service image
docker compose build risk-underwriting-service

# Reset local databases (destructive to the Docker-only volume)
docker compose down -v
```

Set `MYSQL_ROOT_PASSWORD` and `INTELLISURE_JWT_SECRET` to local-only values before starting. The Compose file has no committed credential fallbacks. Keep these values outside the repository, for example in your shell session or an untracked `.env` file.

## Frontend

The shell and remote MFEs continue to use the existing native Angular workflow and `http://localhost` service addresses. When the frontend dependencies are available, start the shell and remotes using the commands in the repository's existing frontend documentation; the Docker stack supplies the Gateway and backend services underneath them.

This Compose file intentionally does not alter `package.json`, `package-lock.json`, Angular configuration, or Windows scripts. The shared Customer & Party configuration uses a numeric JWT expiration value required by Spring Boot; that application defect is fixed independently of Docker.
