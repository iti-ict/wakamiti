# encoding: UTF-8
@ID-Test7
Feature: Test 7 - Lifecycle hooks

Background:
  Given the set of real numbers ℝ

@Before
Scenario: Setup feature execution
  Given a number with value 4 and another number with value 5
  When both numbers are multiplied
  Then the result is equals to 20

@ID-Test7_Scenario1
Scenario: Functional scenario
  Given a number with value 8.02 and another number with value 9
  When both numbers are multiplied
  Then the result is equals to 72.18

@After
Scenario: Teardown feature execution
  Given a number with value 4 and another number with value 4
  When both numbers are multiplied
  Then the result is equals to 16
