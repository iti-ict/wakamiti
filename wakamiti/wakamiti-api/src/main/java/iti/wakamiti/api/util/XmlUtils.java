/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.api.util;

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

public class XmlUtils {

    public static XmlObject xml(String input) {
        return xml(new ByteArrayInputStream(input.getBytes()));
    }

    public static XmlObject xml(InputStream input) {
        try {
            return XmlObject.Factory.parse(input);
        } catch (Exception e) {
            throw new XmlRuntimeException(e);
        }
    }

    public static XmlObject xml(Node input) {
        try {
            return XmlObject.Factory.parse(input);
        } catch (XmlException e) {
            throw new XmlRuntimeException(e);
        }
    }

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
                element.setTextContent(value.toString());
            }
            current.appendChild(element);
        });
    }

    public static String readStringValue(XmlObject obj, String expression) {
        List<String> result = new LinkedList<>();
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            XPathExpression xPathExpression = xPath.compile(expression);
            XPathEvaluationResult<?> evaluationResult = xPathExpression.evaluateExpression(obj.getDomNode());
            if (List.of(NUMBER, STRING, BOOLEAN).contains(evaluationResult.type())) {
                return String.valueOf(evaluationResult.value());
            }

            NodeList nodes = (NodeList) xPathExpression.evaluate(obj.getDomNode(), XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.TEXT_NODE) {
                    result.add(node.getNodeValue());
                } else {
                    result.add(xml(node).xmlText());
                }
            }
        } catch (XPathExpressionException e) {
            throw new XmlRuntimeException(e);
        }
        return result.size() > 1 ? result.toString() : result.stream().findFirst().orElse("");
    }

}
