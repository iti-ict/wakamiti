@Test2
Feature: Test 2 - Scenario Outline

# Comment on the scenario outline
# propertyScenarioOutline: A
  @ID-ScenarioOutline1
  Scenario Outline: Test Scenario Outline
  This is a description for <a>
    Given a number with value <a> and another number with value <b>
    When both numbers are multiplied
    Then the result is equals to <c>
