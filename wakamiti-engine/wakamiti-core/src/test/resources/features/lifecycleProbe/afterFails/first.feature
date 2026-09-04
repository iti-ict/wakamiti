# language: en
Feature: Feature with failing after hook

  Scenario: Functional scenario
    Given an executable lifecycle probe

  @After
  Scenario: Failing cleanup
    Given a failing lifecycle probe
