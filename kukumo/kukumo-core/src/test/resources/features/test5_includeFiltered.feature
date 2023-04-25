# encoding: UTF-8
@Test5
Feature: Test 5 - Include Filteres


@A
Scenario: Test Scenario A
  Given a number with value 8.02 and another number with value 9
  When both numbers are multiplied
  Then the result is equals to 72.18

@B
Scenario: Test Scenario B
  Given a number with value 8.09 and another number with value 9
  When both numbers are multiplied
  Then the result is equals to 72.81