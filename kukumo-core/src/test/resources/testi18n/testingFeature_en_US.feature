# language: en
Feature: Test feature

Scenario: Test scenario
 Given a number with value 7.0 and another number with value 12
 When both numbers are multiplied
 Then the result is equals to 84.0

Scenario Outline: Esquema de escenario de test
 Given a number with value <a> and another number with value <b>
 When both numbers are multiplied
 Then the result is equals to <c>
 Examples:
 | a   | b | c    |
 | 1.0 | 2 |  2.0 |
 | 2.0 | 3 |  6.0 |
 | 5.0 | 4 | 20.0 |



Scenario: Test with table
 Given a number with value 3.1 and the following data:
  | 1.1 |
  | 2.1 |
  | 3.1 | 
 When the table is multiplied by the number
 Then the result is the following:
  | 3.41 |
  | 6.51 |
  | 9.61 |
  
Scenario: Test with text  
  Given the word cucumber and the following paragraph:
  """
  cucumber is the perfect vegetable
  cucumbers are green
  cucumber is also a testing library
  """
  Then each line starts with the given word
  