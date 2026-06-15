# Tutorial Docker

Este ejemplo ejecuta Wakamiti contra la aplicación Petclinic desplegada en Docker.

## Requisitos

- Docker y Docker Compose
- acceso a la imagen `wakamiti/wakamiti`

## Levantar la aplicación

```shell
docker compose up -d
```

Servicios útiles:

- Swagger: `http://localhost:9966/petclinic`
- Base de datos:

```text
url=jdbc:mysql://localhost:3309/petclinic?useUnicode=true
username=root
password=petclinic
```

Log de la aplicación:

```shell
docker logs -f app-petclinic
```

## Lanzar Wakamiti

### Windows

```shell
docker run --rm -v "%cd%/wakamiti:/wakamiti" wakamiti/wakamiti
```

Generación de features desde OpenAPI:

```shell
docker run --rm -v "%cd%/wakamiti:/wakamiti" wakamiti/wakamiti -a -D http://host.docker.internal:9966/petclinic/v2/api-docs -p testgen -L es -t %TOKEN%
```

### Linux

```shell
docker run --rm -v "$(pwd)/wakamiti:/wakamiti" --add-host=host.docker.internal:host-gateway wakamiti/wakamiti
```

## Limpiar contenedores del ejemplo

```shell
docker rm -f app-petclinic mysql-petclinic
```
