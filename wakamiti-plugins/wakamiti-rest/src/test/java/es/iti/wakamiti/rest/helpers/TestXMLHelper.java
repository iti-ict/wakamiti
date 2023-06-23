/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.rest.helpers;


import org.junit.ComparisonFailure;
import org.junit.Test;

import es.iti.wakamiti.rest.MatchMode;



public class TestXMLHelper {

    private final XMLHelper xmlHelper = new XMLHelper();


    @Test
    public void testIdentical() {
        String xml = "<user><name>User</name><age>12</age><contact><mail>user@mail</mail></contact></user>";
        xmlHelper.assertContent(xml, xml, MatchMode.STRICT);
    }


    @Test(expected = ComparisonFailure.class)
    public void testNotIdentical() {
        String expected = "<user><name>User</name><age>12</age><contact><mail>user@mail</mail></contact></user>";
        String actual = "<user><name>User</name><contact><mail>user@mail</mail></contact></user>";
        xmlHelper.assertContent(expected, actual, MatchMode.STRICT);
    }


    @Test
    public void testContains() {
        String expected = "<data><name>User One</name></data>";
        String actual = " <data><id>user1</id><name>User One</name><age>11</age><vegetables><id>1</id><description>Cucumber</description></vegetables><vegetables><id>2</id><description>Gherkin</description></vegetables><contact><mail>user1@mail</mail></contact></data>";
        xmlHelper.assertContent(expected, actual, MatchMode.LOOSE);
    }


    @Test(expected = ComparisonFailure.class)
    public void testContainsReverse() {
        String expected = "<user><name>User</name><age>12</age><contact><mail>user@mail</mail></contact></user>";
        String actual = "<user><name>User</name></user>";
        xmlHelper.assertContent(expected, actual, MatchMode.LOOSE);
    }


    @Test
    public void testIdenticalAnyoOrder() {
        String xml = "<user><name>User</name><age>12</age><contact><mail>user@mail</mail></contact></user>";
        xmlHelper.assertContent(xml, xml, MatchMode.STRICT_ANY_ORDER);
    }


    @Test
    public void testNotIdenticalAnyOrder() {
        String expected = "<user><name>User</name><age>12</age><contact><mail>user@mail</mail></contact></user>";
        String actual = "<user><age>12</age><name>User</name><contact><mail>user@mail</mail></contact></user>";
        xmlHelper.assertContent(expected, actual, MatchMode.STRICT_ANY_ORDER);
    }


    @Test(expected = ComparisonFailure.class)
    public void testDifferentAnyOrder() {
        String expected = "<user><name>User</name><age>12</age><contact><mail>user@mail</mail></contact></user>";
        String actual = "<user><age>12</age><name>UserA</name><contact><mail>user@mail</mail></contact></user>";
        xmlHelper.assertContent(expected, actual, MatchMode.STRICT_ANY_ORDER);
    }

}