# appium.capabilities.appActivity: es.consum.appconsumeventos.MainActivity
Feature: Testing Appium steps

  Scenario: Test 1
    Given the UI element with type 'android.widget.EditText'
    When the text 'Hello World!!!' is typed on that element
    Then that element contains the value 'Hello World!'
