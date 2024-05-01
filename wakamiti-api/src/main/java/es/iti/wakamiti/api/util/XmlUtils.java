/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlRuntimeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static javax.xml.xpath.XPathEvaluationResult.XPathResultType.*;


/**
 * Utility class for working with XML data.
 *
 * <p>This class provides methods for parsing XML from strings, InputStreams, Nodes, and Map representations.
 * It also includes a method for reading string values from an XmlObject based on an XPath expression.</p>
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
public class XmlUtils {

    private XmlUtils() {

    }

    /**
     * Parses the given XML string into an XmlObject.
     *
     * @param input The XML string to parse.
     * @return The parsed XmlObject.
     * @throws XmlRuntimeException If there is an issue parsing the XML string.
     */
    public static XmlObject xml(String input) {
        return xml(new ByteArrayInputStream(input.getBytes()));
    }

    /**
     * Parses the XML content from the given InputStream into an XmlObject.
     *
     * @param input The InputStream containing the XML content.
     * @return The parsed XmlObject.
     * @throws XmlRuntimeException If there is an issue reading or parsing the XML content.
     */
    public static XmlObject xml(InputStream input) {
        try {
            return XmlObject.Factory.parse(input);
        } catch (Exception e) {
            throw new XmlRuntimeException(e);
        }
    }

    /**
     * Parses the given Node into an XmlObject.
     *
     * @param input The Node to parse.
     * @return The parsed XmlObject.
     * @throws XmlRuntimeException If there is an issue parsing the Node.
     */
    public static XmlObject xml(Node input) {
        try {
            return XmlObject.Factory.parse(input);
        } catch (XmlException e) {
            throw new XmlRuntimeException(e);
        }
    }

    /**
     * Creates an XmlObject from a Map representation of XML data.
     *
     * @param rootName The root element name.
     * @param map      The Map representing the XML data.
     * @return The created XmlObject.
     * @throws XmlRuntimeException If there is an issue creating the XmlObject.
     */
    public static XmlObject xml(String rootName, Map<String, Object> map) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = doc.createElement(rootName);
            doc.appendChild(root);

            processMap(doc, root, map);

            return xml(doc);
        } catch (ParserConfigurationException e) {
            throw new XmlRuntimeException(e);
        }
    }


    @SuppressWarnings("unchecked")
    private static void processMap(Document doc, Element current, Map<String, Object> map) {
        map.forEach((key, value) -> {
            Element element = doc.createElement(key);
            if (value instanceof Node) {
                value = xml((Node) value);
            }
            if (value instanceof XmlObject) {
                Node node = doc.importNode(((XmlObject) value).getDomNode().getFirstChild(), true);
                element.appendChild(node);
            } else if (value instanceof Map) {
                processMap(doc, element, (Map<String, Object>) value);
            } else {
                element.setTextContent(value == null ? null : value.toString());
            }
            current.appendChild(element);
        });
    }

    /**
     * Reads a string value from the given XmlObject based on the specified XPath expression.
     *
     * @param obj        The XmlObject from which to read the value.
     * @param expression The XPath expression specifying the value to read.
     * @return The string value read from the XmlObject.
     */
    public static String readStringValue(XmlObject obj, String expression) {
        List<String> results = new LinkedList<>();
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            if (!expression.contains("/")) {
                throw new XPathExpressionException(expression);
            }
            XPathExpression xPathExpression = xPath.compile(expression);
            XPathEvaluationResult<?> evaluationResult = xPathExpression.evaluateExpression(obj.getDomNode());
            if (List.of(NUMBER, STRING, BOOLEAN).contains(evaluationResult.type())) {
                return String.valueOf(evaluationResult.value());
            }

            NodeList nodes = (NodeList) xPathExpression.evaluate(obj.getDomNode(), XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.TEXT_NODE) {
                    results.add(node.getNodeValue());
                } else {
                    results.add(xml(node).xmlText());
                }
            }
        } catch (XPathExpressionException e) {
            Binding binding = new Binding();
            binding.setVariable("obj", obj.toString());
            binding.setVariable("exp", expression);
            GroovyShell shell = new GroovyShell(binding);
            String exp = (obj.schemaType().finalList() && expression.matches("\\[\\d+].*") ? "'x'" : "'x.'") + " + exp";
            Object result = shell.evaluate("Eval.x(new groovy.xml.XmlSlurper().parseText(obj), " + exp + ")");
            return result == null || result.toString().isEmpty() ? null : result.toString();
        }
        return results.size() > 1 ? results.toString() : results.stream().findFirst().orElse(null);

    }

}
