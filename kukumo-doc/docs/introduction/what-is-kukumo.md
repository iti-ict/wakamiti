---
title: ¿Qué es Kukumo?
date: 2022-09-20
slug: introduction/what-is-kukumo
---


> **Conocimiento básico** <br />
> Esta guía asume que conoce los conceptos básicos de [Cucumber](https://cucumber.io/docs/guides/overview/), 
> [Gherkin](https://cucumber.io/docs/gherkin/) y metodología [Behaviour-Driven Development](https://cucumber.io/docs/bdd/).


**Kukumo** es una herramienta de pruebas automáticas inspirada en Cucumber y escrita en Java.

Al igual que Cucumber, puede definir sus pruebas utilizando un lenguaje natural y legible por humanos adoptando, por 
ejemplo, la *gramática* Gherkin. Sin embargo, Kukumo no vincula cada paso a su código de prueba, sino que los pasos 
están vinculados a un código reutilizable de propósito común proporcionado por plugins externos. Por lo tanto, Kukumo 
resulta ser una herramienta conveniente si su objetivo es probar su sistema a través de accesos estandarizados como 
servicios web REST o conexión JDBC, que tienden a ser un gran porcentaje de las pruebas escritas para la mayoría de las 
aplicaciones. No es necesario escribir ningún código de prueba, por lo que incluso los no programadores pueden definir y 
ejecutar sus propias pruebas.

Otras características proporcionadas por Kukumo son:

- **Lanzadores alternativos**: ejecute Kukumo como un conjunto de pruebas de JUnit, como un objetivo de verificación de 
- Maven o directamente como un comando de consola.
- **Totalmente internacionalizable**: puede usar su propio idioma siempre que proporcione a Kukumo un archivo de 
- traducción.
- **Fácilmente extensible**: escriba sus propios plugins para cualquier punto de extensión (pasos, reporters, 
- analizadores de idioma, etc.) y compártalos con la comunidad.
- **No solo Gherkin**: los archivos de características de Gherkin son la fuente de definición de prueba inicial 
- implementada, pero no están vinculados internamente a Kukumo; cualquier plugin puede proporcionar otras formas de 
- recopilar las definiciones de prueba que se utilizarán.


> **RECUERDA** <br />
> Kukumo es una herramienta, no un *framework de test*. Úsalo cuando se adapte a las circunstancias. Tampoco es un 
> sustituto de Cucumber: según tus necesidades, ¡puedes usar *ambos*! 
