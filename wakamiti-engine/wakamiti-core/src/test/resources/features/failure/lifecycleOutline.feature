# encoding: UTF-8
Feature: Lifecycle tags in scenario outline

  @Before
  Scenario Outline: Invalid lifecycle outline
    Given a number with value <a> and another number with value <b>
    When both numbers are multiplied
    Then the result is equals to <result>

    Examples:
      | a | b | result |
      | 2 | 3 | 6      |
