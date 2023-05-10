---
title: ¿Qué es Wakamiti?
date: 2022-09-20
slug: /introduction/what-is-wakamiti
---


> **Conocimiento básico** <br />
> Esta guía asume que conoce los conceptos básicos de [Cucumber](https://cucumber.io/docs/guides/overview/), 
> [Gherkin](https://cucumber.io/docs/gherkin/) y metodología [Behaviour-Driven Development](https://cucumber.io/docs/bdd/).


**Wakamiti** es una herramienta de pruebas automáticas inspirada en Cucumber, escrita en Java y enfocada a las pruebas de 
caja negra usando el lenguaje natural.

Al igual que Cucumber, puede definir sus pruebas utilizando un lenguaje natural y legible por humanos adoptando, por 
ejemplo, la *gramática* Gherkin. Sin embargo, Wakamiti no vincula cada paso a su código de prueba, sino que los pasos 
están vinculados a un código reutilizable de propósito común proporcionado por plugins externos. 

Por lo tanto, Wakamiti resulta ser una herramienta conveniente si su objetivo es probar su sistema a través de accesos 
estandarizados como servicios web REST o conexión JDBC, que tienden a ser un gran porcentaje de las pruebas escritas 
para la mayoría de las aplicaciones. Además, no es necesario escribir ningún código de prueba, por lo que incluso los no 
programadores pueden definir y ejecutar sus propias pruebas.

Otras características proporcionadas por Wakamiti son:

- **Gherkin de dos capas**: puede hacer uso de la gramática Gherkin en dos niveles de abstracción. Uno dirigido a la 
comunicación con el cliente y otro dirigido a los detalles del sistema.
- **Totalmente localizable**: puede usar su propio idioma siempre que proporcione a Wakamiti un archivo de traducción.
- **Fácilmente extensible**: escriba sus propios plugins para cualquier punto de extensión (pasos, generador de informes, 
analizadores de idioma, etc) y compártalos con la comunidad.
- **Lanzadores alternativos**: ejecute Wakamiti como un conjunto de pruebas de JUnit, como un objetivo de verificación de 
Maven o directamente como un comando de consola.


> **RECUERDA** <br />
> Wakamiti es una herramienta, no un *framework de test*. Úsalo cuando se adapte a las circunstancias. Tampoco es un 
> sustituto de Cucumber: según tus necesidades, ¡puedes usar *ambos*! 
