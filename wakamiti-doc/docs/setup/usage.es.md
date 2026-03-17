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
