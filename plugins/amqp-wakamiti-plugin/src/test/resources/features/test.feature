Feature: Test AMQP steps

Scenario Outline: Test AMQP scenario

    Given the AMQP connection URL 'amqp://127.0.0.1:<port>' using the user 'guest' and the password 'guest'
    And the AMQP protocol <protocol>
    And the destination queue TEST
    When the following JSON message is sent to the queue TEST:
    """json
    {
        "data": {
            "message": "Test message sent"
        }
    }
    """
    And wait for 100 milliseconds
    Then the following JSON message is received within 5 seconds:
    """json
    {
        "data": {
            "message": "Test message sent"
        }
    }
    """

    Examples:
     | protocol   | port |
     | AMQP_0_9_1 | 5671 |
     | AMQP_1_0   | 5672 |


Scenario: Purge queue removes pending messages
    Given the AMQP connection URL 'amqp://127.0.0.1:5671' using the user 'guest' and the password 'guest'
    And the AMQP protocol AMQP_0_9_1
    And the destination queue TEST
    When the following JSON message is sent to the queue TEST:
    """json
    {
        "data": {
            "message": "msg-1"
        }
    }
    """
    And the following JSON message is sent to the queue TEST:
    """json
    {
        "data": {
            "message": "msg-2"
        }
    }
    """
    And the message from the JSON file '${test.data}/test.json' is sent to the queue TEST
    And the message from the JSON file '${test.data}/test.json' is received within 2 seconds
    And the queue TEST is emptied
    Then no message is received within 5 seconds


Scenario: Partial JSON message validation
    Given the AMQP connection URL 'amqp://127.0.0.1:5671' using the user 'guest' and the password 'guest'
    And the AMQP protocol AMQP_0_9_1
    And the destination queue TEST
    When the following JSON message is sent to the queue TEST:
    """json
    {
        "data": {
            "message": "msg-3",
            "code": 200
        },
        "meta": {
            "source": "it"
        }
    }
    """
    Then the following JSON fragment is received within 5 seconds:
    """json
    {
        "data": {
            "message": "msg-3"
        }
    }
    """


Scenario: Any-order JSON message validation
    Given the AMQP connection URL 'amqp://127.0.0.1:5671' using the user 'guest' and the password 'guest'
    And the AMQP protocol AMQP_0_9_1
    And the destination queue TEST
    When the following JSON message is sent to the queue TEST:
    """json
    {
        "items": [
            { "id": 1 },
            { "id": 2 }
        ]
    }
    """
    Then the following JSON message is received within 5 seconds (in any order):
    """json
    {
        "items": [
            { "id": 2 },
            { "id": 1 }
        ]
    }
    """
