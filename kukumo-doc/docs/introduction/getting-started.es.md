---
title: Primeros pasos
date: 2022-09-20
slug: /introduction/getting-started
---



### 1. Instala Kukumo

Descarga la imagen docker de Kukumo:
```shell
docker pull kukumo/kukumo
```

[//]: # (Consulta otras [opciones de instalación]&#40;setup/installation&#41;)

### 2. Ejecuta los tests

Ubícate en el directorio que contiene los tests de Kukumo y lánzalo:
```shell
cd ~/test
docker run --rm -it -v "$(pwd):/kukumo" kukumo/kukumo
```

[//]: # (Consulta las [instrucciones de uso]&#40;setup/usage&#41; para conocer todos los comandos y opciones.)

