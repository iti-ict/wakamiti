# language: en
# modules: rest-steps
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
 And the response is equal to the file 'src/test/resources/user1.xml'
 And the response contains:
 """
 <data>
  <name>User One</name>
 </data>
 """
 And the text from response fragment 'data.contact.email' is 'user1@mail'
 And the response satisfies the following schema:
 """xml
 <?xml version="1.0" encoding="UTF-8"?>
 <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified">
   <xs:element name="data">
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
         <xs:element ref="id"/>
         <xs:element ref="description"/>
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