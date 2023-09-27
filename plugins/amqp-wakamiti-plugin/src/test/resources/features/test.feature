Feature: Test AMQP steps

    Scenario: Test AMQP scenario

        Given the destination queue TEST
        When the following JSON message is sent to the queue TEST:
    ```json
    {
    "data": {
    "message": "Test message sent"
    }
    }
    ```
        Then the following JSON message is received within 5 seconds:
    ```json
    {
    "data": {
    "message": "Test message sent"
    }
    }
    ```