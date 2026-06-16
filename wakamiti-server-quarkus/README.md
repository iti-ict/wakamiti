# Wakamiti Server Quarkus

`wakamiti-server-quarkus` is an HTTP execution server plus a TCP language-server host built on Quarkus.

It is currently an independent module in the repository and is not included in the root aggregator build by default. Build it explicitly when you need it.

## What it exposes

- HTTP API on port `8080`
- TCP language server on port `8090`
- Swagger UI at `http://localhost:8080/q/swagger-ui`
- OpenAPI document at `http://localhost:8080/q/openapi`

Main HTTP resources in the current codebase:

- `GET /tokens`
- `POST /plans`
- `POST /executions`
- `GET /executions`
- `GET /executions/{executionID}`
- `GET /wakamiti/configuration`
- `GET /wakamiti/contributors`

`/plans` and `/executions` require authentication; `/tokens` is the bootstrap entry point for obtaining a JWT token.

## Build

```bash
./mvnw -f wakamiti-server-quarkus/pom.xml package
```

## Run in development mode

```bash
./mvnw -f wakamiti-server-quarkus/pom.xml quarkus:dev
```

## Run the packaged server

The module packages a runner JAR plus a `target/lib/` directory, and the Dockerfile starts the custom `Runner` main class with both on the classpath.

In practice, the most reliable local entry point is still:

```bash
./mvnw -f wakamiti-server-quarkus/pom.xml quarkus:dev
```

If you need to launch the packaged output manually, use the generated `*-runner.jar` file name under `target/` together with `target/lib/*`.

## Docker image

The module ships a Dockerfile that expects the packaged artifacts in `target/`:

```bash
./mvnw -f wakamiti-server-quarkus/pom.xml package
docker build -t wakamiti-server-quarkus:local wakamiti-server-quarkus
docker run --rm -p 8080:8080 -p 8090:8090 --name wakamiti-server wakamiti-server-quarkus:local
```

To add extra runtime modules, mount them into `/app/lib-ext`:

```bash
docker run --rm \
  -p 8080:8080 \
  -p 8090:8090 \
  -v /path/to/modules:/app/lib-ext \
  --name wakamiti-server \
  wakamiti-server-quarkus:local
```
