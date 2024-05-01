Feature: Test AMQP steps

Scenario: Test AMQP scenario

    Given the AMQP connection URL 'amqp://127.0.0.1:5671' using the user 'guest' and the password 'guest'
    And the destination queue TEST
    When the following JSON message is sent to the queue TEST:
    """json
    {
        "data": {
            "message": "Test message sent"
        }
    }
    """
    Then the following JSON message is received within 5 seconds:
    """json
    {
        "data": {
            "message": "Test message sent"
        }
    }
    """