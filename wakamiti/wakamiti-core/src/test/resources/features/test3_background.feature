# encoding: UTF-8
@Test3
Feature: Test 3 - Backgrounds

Background:
  Given the set of real numbers ℝ


@ID-Test3_Scenario1
Scenario: Test Scenario 1
  Given a number with value 8.02 and another number with value 9
  When both numbers are multiplied
  Then the result is equals to 72.18
 
@ID-Test3_Scenario2
Scenario: Test Scenario 2
  Given a number with value 7.02 and another number with value 8
  When both numbers are multiplied
  Then the result is equals to 56.16
 
@ID-Test3_Scenario3
Scenario: Test Scenario 3
  Given a number with value 8.09 and another number with value 9
  When both numbers are multiplied
  Then the result is equals to 72.81