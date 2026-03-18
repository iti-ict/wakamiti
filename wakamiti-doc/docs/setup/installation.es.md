---
title: Instalación
date: 2022-09-20
slug: /setup/installation
---

Esta guía explica cómo ejecutar Wakamiti rápidamente y comprobar que la instalación funciona.

## Requisitos previos

- Tener [Docker](https://www.docker.com/get-started/) instalado y en ejecución.
- Disponer de un directorio de proyecto con tus ficheros `.feature` y `wakamiti.yaml`.
- Tener acceso a Internet para descargar la imagen de Docker y dependencias externas.

## Ejecutar Wakamiti con Docker

Desde el directorio del proyecto, ejecuta:

Windows:
```shell copy=true
docker run --rm -v "%cd%:/wakamiti" wakamiti/wakamiti
```

Linux:
```shell copy=true
docker run --rm -v "$(pwd):/wakamiti" --add-host=host.docker.internal:host-gateway wakamiti/wakamiti
```

## Usar una versión específica de Wakamiti

Si necesitas fijar una versión concreta para ejecuciones reproducibles:

```shell copy=true
docker run --rm -v "%cd%:/wakamiti" wakamiti/wakamiti:<version>
```

Etiquetas disponibles: [Docker Hub de Wakamiti](https://hub.docker.com/r/wakamiti/wakamiti/tags)

## Validar la instalación

Tras la ejecución, comprueba:

- Que el proceso termina mostrando resultados en consola.
- Que se generan los ficheros de salida (por ejemplo, `wakamiti.json` y `wakamiti.html` si el reporte está habilitado).

## Siguientes pasos

- Revisa la configuración recomendada en [Configuración](setup/configuration).
- Sigue el flujo de ejecución en [Uso](setup/usage).
- Consulta todas las opciones del motor en [Arquitectura](wakamiti/architecture).
