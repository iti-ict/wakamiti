#@Azure

# azurePlan: Wakamiti Test Plan
# azureArea: ACS
# azureIteration: ACS\\Iteración 1
# azureSuite: Wakamiti Test Suite A
Feature: Azure integration feature


  @ID-1
    # azureTest: Wakamiti Scenario B - Fail
  Scenario: Impl Test Case B
    * this step fails


  @ID-2
  Scenario: Impl Test Case C
      # azureTest: Wakamiti Scenario C - Fail
    * this step fails


  @ID-3
      # azureTest: Wakamiti Scenario A - Fail
  Scenario: Def Test Case A
    * this step do nothing

