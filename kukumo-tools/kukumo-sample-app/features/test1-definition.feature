#language: es

@definition
Característica: Prueba rápida de servicios

@ID-1
Esquema del escenario: Buscar paises según una IP
 Dada la IP <ip>
 Cuando se consulta a qué país corresponde
 Entonces responde el país <pais>
 Ejemplos:
 | ip       | pais      |
 | 5.6.7.8  | Germany   |
 | 14.2.3.4 | Australia |
 | 27.1.1.7 | Spain     |
