---
title: Desarrollo de plugins
date: 2026-03-30
slug: /plugins/development
---

## Enfoque recomendado

- Crea y versiona tu plugin (por ejemplo, `myplugin-wakamiti-plugin`).
- Publica el artefacto en un repositorio Maven (local, Nexus, Artifactory o Maven Central).
- Cárgalo en Wakamiti como módulo externo.

## Tipos de plugin

### Plugin de pasos (`steps`)

Úsalo cuando quieras aportar un catálogo de pasos Gherkin.

Qué incluir:

- Una clase que implemente `StepContributor` con métodos anotados con `@Step`.
- Un `ConfigContributor<TuStepContributor>` si necesitas configuración por defecto o wiring.
- Ficheros `.properties` para mapear claves de `@Step` a frases Gherkin.

Si defines módulo Java (`module-info.java`), registra los `provides`:

```java
provides es.iti.wakamiti.api.extensions.StepContributor with com.mycompany.MyStepContributor;
provides es.iti.wakamiti.api.extensions.ConfigContributor with com.mycompany.MyStepConfigContributor;
```

### Plugin de informes (`report`)

Úsalo cuando quieras generar salida de resultados (HTML, JSON, integración externa, etc.).

Qué incluir:

- Una clase que implemente `Reporter` y su método `report(PlanNodeSnapshot rootNode)`.
- Un `ConfigContributor<TuReporter>` para parámetros de salida, rutas o estrategia de generación.

Si defines módulo Java (`module-info.java`), registra los `provides`:

```java
provides es.iti.wakamiti.api.extensions.Reporter with com.mycompany.MyReportGenerator;
provides es.iti.wakamiti.api.extensions.ConfigContributor with com.mycompany.MyReportConfigContributor;
```

## Generar la base del plugin

Puedes usar el archetype oficial:

```shell copy=true
mvn archetype:generate -DarchetypeGroupId=es.iti.wakamiti -DarchetypeArtifactId=wakamiti-plugin-maven-archetype -DarchetypeVersion=1.1.0
```

Propiedades clave del asistente:

- `groupId`: grupo Maven de tu organización.
- `artifactId`: artefacto Maven del plugin.
- `pluginId`: prefijo funcional del plugin (minúsculas, estable).
- `wakamitiApiVersion` y `wakamitiCoreVersion`: versiones compatibles con tu stack de ejecución.

## Publicar y consumir desde fuera

Una vez empaquetado y publicado tu plugin, añádelo como módulo externo.

Ejemplo en `wakamiti.yaml`:

```yaml
wakamiti:
  launcher:
    modules:
      - com.mycompany.wakamiti:myplugin-wakamiti-plugin:1.0.0
```

Más detalle sobre resolución de módulos: [`wakamiti.launcher.modules`](wakamiti/architecture#wakamitilaunchermodules).

## Checklist de publicación

- Versionado semántico (`1.0.0`, `1.1.0`, ...).
- CI que ejecute tests y publique artefactos.
- `CHANGELOG.md` y documentación de configuración del plugin.
- Compatibilidad explícita de versiones Wakamiti soportadas.
