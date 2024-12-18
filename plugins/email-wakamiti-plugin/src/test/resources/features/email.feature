Feature: Email server capabilities

  Background:
    Given the email folder 'INBOX'
    * At finish, delete all emails whose sender is 'senger@localhost'


  Scenario: Check last received text email
    Given that the number of unread emails is 0
    * a text email is sended
    Then the number of unread emails is 1
    And the subject of the email is 'Test Subject'
    And the sender of the email is 'sender@localhost'
#    And the body of the email is:
#    ```
#    Test Body
#    ```
    And the number of attachments in the email is 0


  Scenario: Check last received email with attachments
    Given that the number of unread emails is 0
    * an email with attachment is sended
    Then the number of unread emails is 1
    And the subject of the email is 'Test Subject'
    And the sender of the email is 'sender@localhost'
#    And the body of the email is:
#    ```
#    Test Body
#    ```
    And the number of attachments in the email is 1
    And the email has an attached file whose name is 'attachment.txt'
#    And the email has an attached file with the following content:
#    ```
#    Attachment
#    ```
