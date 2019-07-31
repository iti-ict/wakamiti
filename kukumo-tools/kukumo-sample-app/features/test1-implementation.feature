#language: en

@implementation
Feature: implementation

Background:
  Given the base URL http://api.ip2country.info

@ID-1
# redefinition.stepMap: 1-1-2
Scenario Outline: Buscar paises según una IP
 Given the IP identified by 'ip?<ip>'
 When the IP is requested
 Then the response HTTP code is 200
 And the response contains:
 """
 { "countryName": "<pais>" }
 """
 Examples:
 | ip       | pais      |
 | 5.6.7.8  | Germany   |
 | 14.2.3.4 | Australia |
 | 27.1.1.7 | Spain     |

