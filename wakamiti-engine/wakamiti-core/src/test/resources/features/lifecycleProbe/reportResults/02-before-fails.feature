# language: en
Feature: Feature with failing before hook

  @Before
  Scenario: Failing setup
    Given a failing lifecycle probe

  Scenario: Functional scenario
    Given an executable lifecycle probe

  @After
  Scenario: Cleanup
    Given an executable lifecycle probe
