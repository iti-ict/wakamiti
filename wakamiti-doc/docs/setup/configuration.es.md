---
title: Configuración
date: 2022-09-20
slug: /setup/configuration
---

Wakamiti se configura mediante un fichero `wakamiti.yaml` ubicado en el directorio del proyecto de pruebas.

## Ubicación del fichero

- Ubicación recomendada: raíz del proyecto de pruebas.
- Nombre por defecto: `wakamiti.yaml`.
- Los recursos de prueba se leen desde la ruta actual salvo que se configure `wakamiti.resourcePath`.

## Ejemplo mínimo de configuración

```yaml copy=true
wakamiti:
  resourceTypes:
    - gherkin
  launcher:
    modules:
      - es.iti.wakamiti:rest-wakamiti-plugin
      - es.iti.wakamiti:html-report-wakamiti-plugin
  htmlReport:
    title: Ejecución de pruebas
  rest:
    baseURL: http://localhost:8080/api
```

## Estructura de la configuración

- `wakamiti`: opciones globales del motor (tipos de recurso, ejecución, logs, salida, etiquetas, etc.).
- `wakamiti.launcher.modules`: módulos externos (plugins, drivers JDBC, extensiones personalizadas).
- Bloques de plugin (`rest`, `database`, `amqp`, `htmlReport`, etc.): opciones específicas de cada plugin.

## Recomendaciones prácticas

- Declara explícitamente `launcher.modules` para evitar dependencias implícitas.
- Mantén fuera del repositorio los valores dependientes de entorno cuando sea posible.
- Empieza con una configuración mínima y añade opciones solo cuando las necesites.
- Si cambian endpoints o datos, utiliza un fichero de configuración por entorno.

## Referencias relacionadas

- Catálogo completo de opciones: [Arquitectura](wakamiti/architecture)
- Opciones por plugin: [Plugins](plugins)
