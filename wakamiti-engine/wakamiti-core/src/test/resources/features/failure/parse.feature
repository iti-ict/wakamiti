```gherkin
@Test1
Feature: Test 1 - Simple Scenario
  This is a simple scenario feature without additional behaviour.
  And this is the second line of comments.

# This is a comment on the scenario
# scenarioProperty: B
  @ID-Test1_Scenario1
  Scenario: Test Scenario
  # This is a comment on the step
  # stepProperty: C
    Given a number with value 8.02 and another number with value 9
    When both numbers are multiplied
    Then the result is equals to 72.18
  # this step is only for error message testing
  # * this stepo has an integer cool, a value 3, and the string starts with 'pen'
```