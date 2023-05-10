@Test5
Feature: Test 5 - Invalid Step

Background:
  Given the set of real numbers ℝ

Scenario Outline: Test Scenario Outline
 Given a number with value <a> and another number with value <b>
 When both numbers are mooltiplied
 Then the result is equals to <c>
 Examples:
 | a   | b |  c   |
 | 1.0 | 2 |  2.0 |
 | 2.0 | 3 |  6.0 |
 | 5.0 | 4 | 20.0 |