@XRay
@definition @ID-1
# xrayPlan: Wakamiti Test Plan
# xrayArea: ACS
# xrayIteration: ACS\\Iteración 1
# xraySuite: Wakamiti Test Suite A \\ Wakamiti Test Suite A1 \\ Wakamiti Test Suite A11
Feature: XRay integration feature


  @ID-1
  # xrayTest: Wakamiti Scenario B - Fail cambio
  Scenario: Wakamiti Scenario B - Fail cambio
    * Def this step fails


  @ID-2
  # xrayTest: Wakamiti Scenario C - Fail
  Scenario: Def Test Case C
    * Def this step fails


  @ID-3
  # xrayTest: Wakamiti Scenario A - Fail
  Scenario: Def Test Case A
    * Def this step do nothing



