# encoding: UTF-8
@TesT4
Feature: Test 4 - Tag Expressions


@A
Scenario: Test Scenario A
  Given a number with value 8.02 and another number with value 9
  When both numbers are multiplied
  Then the result is equals to 72.18
 
@A @B
Scenario: Test Scenario A B
  Given a number with value 7.02 and another number with value 8
  When both numbers are multiplied
  Then the result is equals to 56.16
 
@B @C
Scenario: Test Scenario B C
  Given a number with value 8.09 and another number with value 9
  When both numbers are multiplied
  Then the result is equals to 72.81