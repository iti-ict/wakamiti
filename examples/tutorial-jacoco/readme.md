# Tutorial Docker con JaCoCo

Este ejemplo amplía el tutorial Docker para recoger cobertura JaCoCo de la aplicación desplegada.

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

## Crear la imagen extendida de Wakamiti

```shell
docker build -t wakamiti-jacoco wakamiti
```

## Lanzar Wakamiti

### Windows

```shell
docker run --rm -v "%cd%/wakamiti:/wakamiti" -v "tutorial-jacoco_app-data:/app/classes" --network wakamiti-net wakamiti-jacoco
```

### Linux

```shell
docker run --rm -v "$(pwd)/wakamiti:/wakamiti" --network wakamiti-net wakamiti-jacoco
```

## Limpiar contenedores del ejemplo

```shell
docker rm -f app-petclinic mysql-petclinic
```
