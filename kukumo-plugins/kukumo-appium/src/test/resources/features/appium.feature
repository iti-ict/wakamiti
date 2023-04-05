Feature: Testing Appium steps

  Scenario: Test 1
    Given the UI element with type 'android.widget.EditText'
    When the text 'Hello World!!!' is typed on that element
    Then that element contains the value 'Hello World!'


  Scenario: Call
    Given an incoming call with number '636828752' is received
    When the incoming call is accepted
    And the call is ended
