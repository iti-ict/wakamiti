/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.TypeRef;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.xml.slurpersupport.NodeChildren;
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
import java.io.*;
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

    private static final XmlMapper MAPPER = XmlMapper.builder()
            .addModule(new JavaTimeModule())
            .addModule(new CustomModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .build();


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

    public static XmlObject xml(Object input) {
        try {
            return xml(MAPPER.writeValueAsString(input));
        } catch (JsonProcessingException e) {
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
            Document doc = newDocument();
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
                value = xml(value);
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

    public static <T> T read(XmlObject obj, String expression, Class<T> type) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            if (!expression.contains("/")) {
                throw new XPathExpressionException(expression);
            }
            XPathExpression xPathExpression = xPath.compile(expression);
            XPathEvaluationResult<?> result = xPathExpression.evaluateExpression(obj.getDomNode());

            if (List.of(NUMBER, STRING, BOOLEAN).contains(result.type())) {
                return MAPPER.convertValue(result.value(), type);
            } else {
                NodeList nodes = (NodeList) xPathExpression.evaluate(obj.getDomNode(), XPathConstants.NODESET);
                if (nodes.getLength() == 1) {
                    return read(xml(nodes.item(0)), type);
                }
                Document doc = newDocument();
                Element element = doc.createElement("root");
                doc.appendChild(element);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    Node copyNode = doc.importNode(node, true);
                    if (node.getNodeType() == Node.TEXT_NODE) {
                        Element it = doc.createElement("item");
                        it.appendChild(copyNode);
                        element.appendChild(it);
                    } else {
                        element.appendChild(copyNode);
                    }
                }
                return MAPPER.readValue(xml(element).toString(), type);
            }
        } catch (XPathExpressionException e) {
            return read(obj, expression, TypeFactory.defaultInstance().constructType(type));
        } catch (ParserConfigurationException | JsonProcessingException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public static <T> T read(XmlObject obj, String expression, TypeRef<T> type) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            if (!expression.contains("/")) {
                throw new XPathExpressionException(expression);
            }
            XPathExpression xPathExpression = xPath.compile(expression);
            return read(xml(xPathExpression.evaluate(obj)), type);
        } catch (XPathExpressionException e) {
            return read(obj, expression, MAPPER.getTypeFactory().constructType(type.getType()));
        }
    }

    public static <T> T read(XmlObject obj, Class<T> type) {
        return MAPPER.convertValue(obj.getDomNode(), type);
    }

    public static <T> T read(XmlObject obj, TypeRef<T> type) {
        return MAPPER.convertValue(obj.getDomNode(), MAPPER.getTypeFactory().constructType(type.getType()));
    }

    private static <T> T read(XmlObject obj, String expression, JavaType type) {
        Binding binding = new Binding();
        binding.setVariable("obj", obj.toString());
        binding.setVariable("exp", expression);
        GroovyShell shell = new GroovyShell(binding);
        String exp = (obj.schemaType().finalList() && expression.matches("\\[\\d+].*") ? "'x'" : "'x.'") + " + exp";
        Object result = shell.evaluate("Eval.x(new groovy.xml.XmlSlurper().parseText(obj), " + exp + ")");
        if (result == null) return null;
        if (result instanceof NodeChildren) {
            StringWriter writer = new StringWriter();
            try {
                ((NodeChildren) result).writeTo(writer);
            } catch (IOException e) {
                throw new XmlRuntimeException(e);
            }
            return MAPPER.convertValue(writer.toString(), type);
        }
        return MAPPER.convertValue(result, type);
    }

    private static Document newDocument() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.newDocument();
    }

    private static class CustomModule extends SimpleModule {

        public CustomModule() {
            super("configModue", com.fasterxml.jackson.core.Version.unknownVersion());
            this.addDeserializer(XmlObject.class, new XmlObjectDeserializer());
            this.addSerializer(NodeChildren.class, new NodeChildSerializer());
        }
    }

    private static class XmlObjectDeserializer extends StdDeserializer<XmlObject> {

        public XmlObjectDeserializer() {
            this(null);
        }

        protected XmlObjectDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public XmlObject deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
            parser.getCodec().readTree(parser); // The result is ignored
            JsonLocation end = parser.getCurrentLocation();
            StringWriter writer = new StringWriter();
            if (end.contentReference().getRawContent() instanceof StringReader) {
                ((StringReader) end.contentReference().getRawContent()).reset();
                ((StringReader) end.contentReference().getRawContent()).transferTo(writer);
            } else {
                writer.append(end.contentReference().getRawContent().toString());
            }
            return xml(writer.toString());
        }
    }

    private static class NodeChildSerializer extends StdSerializer<NodeChildren> {

        public NodeChildSerializer() {
            this(null);
        }

        protected NodeChildSerializer(Class<NodeChildren> t) {
            super(t);
        }

        @Override
        public void serialize(NodeChildren xml, JsonGenerator generator, SerializerProvider provider) throws IOException {
            StringWriter writer = new StringWriter();
            xml.writeTo(writer);
            generator.writeString(writer.toString());
        }
    }
}
