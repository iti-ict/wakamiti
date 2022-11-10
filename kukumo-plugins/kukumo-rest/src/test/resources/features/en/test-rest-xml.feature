# language: en
Feature: REST Test Feature

  Background:
    Given the base URL http://localhost:8888
    And the REST service '/users'
    And the REST content type XML

  Scenario: Get a user from a service
    Given a user identified by 'user1'
    When the user is requested
    Then the response HTTP code is greater than or equals to 200
    And the response HTTP code is less than 500
    And the response content type is XML
    And the response is equal to the file 'src/test/resources/server/users/user1.xml'
    And the response contains:
      """
<item>
    <name>User One</name>
</item>
      """
    And the text from response fragment 'data.contact.email' is 'user1@mail'
    And the response satisfies the following schema:
      """xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified">
    <xs:element name="item">
        <xs:complexType mixed="true">
            <xs:choice minOccurs="0" maxOccurs="unbounded">
                <xs:element ref="id"/>
                <xs:element ref="age"/>
                <xs:element ref="contact"/>
                <xs:element ref="name"/>
                <xs:element ref="vegetables"/>
            </xs:choice>
        </xs:complexType>
    </xs:element>
    <xs:element name="age">
        <xs:simpleType>
            <xs:restriction base="xs:integer">
                <xs:minInclusive value="5"/>
            </xs:restriction>
        </xs:simpleType>
    </xs:element>
    <xs:element name="contact">
        <xs:complexType>
            <xs:sequence>
                <xs:element ref="email"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>
    <xs:element name="email">
        <xs:simpleType>
            <xs:restriction base="xs:string">
                <xs:pattern value="[a-zA-Z0-9]+@[a-zA-Z0-9\.]+"/>
            </xs:restriction>
        </xs:simpleType>
    </xs:element>
    <xs:element name="name" type="xs:string"/>
    <xs:element name="vegetables">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="LinkedList">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="item" maxOccurs="unbounded" minOccurs="0">
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element ref="id"/>
                                        <xs:element ref="description"/>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
            </xs:sequence>
        </xs:complexType>
    </xs:element>
    <xs:element name="description" type="xs:NCName"/>
    <xs:element name="id">
        <xs:simpleType>
            <xs:restriction base="xs:string">
                <xs:pattern value="[a-zA-Z0-9]+"/>
            </xs:restriction>
        </xs:simpleType>
    </xs:element>
</xs:schema>
      """

  Scenario: URL with parameters
    Given the REST service '/users/{user}/{subject}'
    And the following path parameters:
      | name    | value      |
      | user    | user1      |
      | subject | vegetables |
    When the subject is queried
    Then the response is:
      """xml
      <ArrayList>
        <item>
            <id>1</id>
            <description>Cucumber</description>
        </item>
        <item>
            <id>2</id>
            <description>Gherkin</description>
        </item>
    </ArrayList>
      """