---
title: Pasos de ficheros
date: 2022-09-20
slug: /plugins/files
---

Este plugin proporciona pasos para operar sobre ficheros y directorios locales durante la ejecución de pruebas. Es útil
en flujos end-to-end donde el sistema crea, modifica o elimina archivos.

---
## Tabla de contenido

---

## Instalación

Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:io-wakamiti-plugin:2.7.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>io-wakamiti-plugin</artifactId>
  <version>2.7.0</version>
</dependency>
```

## Configuración

### `files.timeout`
- Tipo: `long`
- Por defecto: `60`

Tiempo máximo de espera (en segundos) usado por los pasos de espera de ficheros.

Ejemplo:
```yaml
files:
  timeout: 120
```

### `files.enableCleanupUponCompletion`
- Tipo: `boolean`
- Por defecto: `false`

Si se habilita, las operaciones de ficheros registradas por el plugin se limpian al finalizar la ejecución.

Ejemplo:
```yaml
files:
  enableCleanupUponCompletion: true
```

### `files.links`
- Tipo: `string`

Define enlaces simbólicos creados durante la fase de inicialización. Formato: `origen=destino`, separados por comas o
punto y coma.

Ejemplo:
```yaml
files:
  links: "./tmp/entrada=./runtime/entrada; ./tmp/salida=./runtime/salida"
```

## Grupos de pasos principales

### Operaciones de fichero

```text copy=true
(que) el (fichero|directorio) {src} se mueve al directorio {dest}
(que) el (fichero|directorio) {src} se mueve al fichero {dest}
(que) el (fichero|directorio) {src} se copia al directorio {dest}
(que) el (fichero|directorio) {src} se copia al fichero {dest}
(que) el (fichero|directorio) {file} se elimina
```

### Operaciones de espera

```text copy=true
(que) se espera a que el (fichero|directorio) {file} se elimine
(que) se espera a que el (fichero|directorio) {file} se modifique
(que) se espera a que el (fichero|directorio) {file} se cree
```

### Comprobaciones

```text copy=true
el fichero {file} existe
el fichero {file} no existe
el fichero {file} contiene el siguiente texto:
el fichero {file} contiene los siguientes datos:
el fichero {file} tiene una longitud de {chars}
```

## Ejemplo de uso

```gherkin
Escenario: El informe generado está disponible
  Dado un tiempo de espera de 120 segundos
  Cuando se espera a que el fichero 'out/report.txt' se cree
  Entonces el fichero 'out/report.txt' existe
```
