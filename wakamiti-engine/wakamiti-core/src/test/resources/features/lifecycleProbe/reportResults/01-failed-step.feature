# language: en
Feature: Feature with failed functional scenario

  @Before
  Scenario: Setup
    Given an executable lifecycle probe

  Scenario: Functional scenario
    Given an assertion-failing lifecycle probe

  @After
  Scenario: Cleanup
    Given an executable lifecycle probe
