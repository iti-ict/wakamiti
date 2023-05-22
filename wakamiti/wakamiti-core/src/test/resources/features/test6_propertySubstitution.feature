# encoding: UTF-8

@Test4
Feature: Test 6 - Property Substitution


Scenario: Test Scenario
  Given a number with value ${number.a} and another number with ${number.b}
  When both numbers are ${operation}
  Then the result is equals to ${number.c}