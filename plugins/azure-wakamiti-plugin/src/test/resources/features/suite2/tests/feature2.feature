
Feature: Azure integration feature


  @ID-s2-f2-1
  Scenario: Scenario B
    * this step do nothing
    * this step do nothing


  @ID-s2-f2-2
  Scenario: Scenario C
    * this step do nothing
    * this step fails


  @ID-s2-f2-3
  Scenario Outline: Scenario A <param>
    * this step fails
    * this step do nothing

    Examples:
      | param |
      | aa    |
      | bb    |