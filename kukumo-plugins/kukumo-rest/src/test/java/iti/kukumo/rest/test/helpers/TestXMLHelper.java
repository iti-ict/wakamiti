package iti.kukumo.rest.test.helpers;

import iti.kukumo.rest.helpers.XMLHelper;
import org.junit.ComparisonFailure;
import org.junit.Test;

/**
 * @author ITI
 * Created by ITI on 18/04/19
 */
public class TestXMLHelper {


    private XMLHelper xmlHelper = new XMLHelper();

    @Test
    public void testIdentical() {
        String xml = "<user><name>User</name><age>12</age><contact><email>user@mail</email></contact></user>";
        xmlHelper.assertContent(xml,xml,true);
    }


    @Test(expected = ComparisonFailure.class)
    public void testNotIdentical() {
        String expected = "<user><name>User</name><age>12</age><contact><email>user@mail</email></contact></user>";
        String actual = "<user><name>User</name><contact><email>user@mail</email></contact></user>";
        xmlHelper.assertContent(expected,actual,true);
    }

    @Test
    public void testContains() {
        String expected = "<data><name>User One</name></data>";
        String actual = " <data><id>user1</id><name>User One</name><age>11</age><vegetables><id>1</id><description>Cucumber</description></vegetables><vegetables><id>2</id><description>Gherkin</description></vegetables><contact><email>user1@mail</email></contact></data>";
        xmlHelper.assertContent(expected,actual,false);
    }


    @Test(expected = ComparisonFailure.class)
    public void testContainsReverse() {
        String expected = "<user><name>User</name><age>12</age><contact><email>user@mail</email></contact></user>";
        String actual = "<user><name>User</name></user>";
        xmlHelper.assertContent(expected,actual,false);
    }

}
