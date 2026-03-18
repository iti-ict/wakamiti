---
title: Uso
date: 2022-09-20
slug: /setup/usage
---

Esta página describe un flujo práctico para ejecutar y mantener pruebas con Wakamiti.

## Flujo de trabajo recomendado

1. Define `wakamiti.yaml` con opciones globales y los plugins necesarios.
2. Escribe ficheros `.feature` con tus escenarios de negocio.
3. Ejecuta Wakamiti desde el directorio del proyecto.
4. Analiza los reportes generados y ajusta escenarios o configuración.

## Estructura de proyecto recomendada

```text
mis-pruebas/
├── wakamiti.yaml
├── features/
│   ├── api/
│   │   └── usuarios.feature
│   └── integracion/
│       └── pedidos.feature
└── data/
    ├── request/
    └── expected/
```

## Ejecutar desde terminal

Windows:
```shell copy=true
docker run --rm -v "%cd%:/wakamiti" wakamiti/wakamiti
```

Linux:
```shell copy=true
docker run --rm -v "$(pwd):/wakamiti" --add-host=host.docker.internal:host-gateway wakamiti/wakamiti
```

## Uso del CLI `wakamiti-launcher`

Si tienes instalado el comando `wakamiti`, puedes ejecutarlo directamente con opciones CLI:

```shell copy=true
wakamiti [opciones]
```

Ejemplos habituales:

```shell copy=true
# Ejecutar usando un fichero de configuración concreto
wakamiti -f wakamiti.ci.yaml

# Añadir módulos desde CLI (separados por coma)
wakamiti -m es.iti.wakamiti:rest-wakamiti-plugin:3.0.0,es.iti.wakamiti:html-report-wakamiti-plugin:3.0.0

# Sobrescribir propiedades de configuración sin tocar el yaml
wakamiti -K tagFilter="@smoke and not @ignore" -K outputFilePath=results/wakamiti.json

# Configurar repositorios Maven en línea
wakamiti -M remoteRepositories="https://repo.maven.apache.org/maven2;file:///C:/Users/usuario/.m2/repository"

Para ver el catálogo completo de opciones CLI y su correspondencia con configuración:
[Arquitectura](wakamiti/architecture).

## Entender los ficheros de resultado

- `wakamiti.json`: resultado de ejecución en formato legible por máquinas, útil para automatización.
- `wakamiti.html`: informe visual para análisis funcional y compartición con el equipo.

## Consejos de uso en equipo

- Mantén los escenarios centrados en comportamiento, no en detalles técnicos internos.
- Reutiliza datos de prueba comunes y patrones de pasos compartidos.
- Versiona conjuntamente los `.feature` y la configuración.
- Trata los escenarios fallidos como una señal de calidad y revísalos cuanto antes.

## Guías relacionadas

- Tutorial rápido: [Primeros pasos](introduction/getting-started)
- Opciones del motor: [Arquitectura](wakamiti/architecture)
- Plugins disponibles: [Plugins](plugins)
