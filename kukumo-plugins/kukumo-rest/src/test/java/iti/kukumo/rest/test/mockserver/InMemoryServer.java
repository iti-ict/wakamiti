/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest.test.mockserver;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;


public class InMemoryServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryServer.class);


    public enum Format {
        JSON, XML
    }

    interface HttpServerFactory {
        HttpServer create(int port, Charset charset, HttpHandler handler);
    }


    interface HttpServer {
        void startServer() throws IOException;
        void stopServer() throws IOException;
    }

    interface HttpHandler {
        void handle(HttpExchange exchange) throws IOException;
    }

    interface HttpExchange {
        String getRequestMethod();
        URI getRequestURI();
        InputStream getRequestBody();
        void setResponseContentType(String string);
        void setResponseCode(int status);
        void setResponseContentLength(int i);
        void setResponseBody(String body);
        void writeResponse() throws IOException;
    }




    private HttpServer server;
    private boolean running = false;
    private JsonNode data;
    private String idProperty;
    private Format format;
    private Charset charset;

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();



    public InMemoryServer(
        HttpServerFactory serverFactory,
        Format format,
        Charset charset,
        int port
    ) throws IOException {
        configure(serverFactory, format, charset, port);
        start();
    }




    public InMemoryServer(
        HttpServerFactory serverFactory,
        Format format,
        Charset charset,
        int port,
        String filename,
        Format fileFormat
    ) throws IOException {
        try {
            configure(serverFactory, format, charset, port);
            loadFromFile(filename, fileFormat);
            start();
        } catch (Exception e) {
            LOGGER.error("Error starting Mock Server", e);
            stop();
            throw e;
        }
    }




    protected void configure(
        HttpServerFactory serverFactory,
        Format format,
        Charset charset,
        int port
    ) throws IOException {
        server = serverFactory.create(port, charset, this::handle);
        data = jsonMapper.createObjectNode();
        idProperty = "id";
        this.format = format;
        this.charset = charset;
    }



    @SuppressWarnings("unchecked")
    public void loadFromInputStream(InputStream stream, Format loadFormat) throws IOException {
        if (running) {
            throw new IllegalStateException("cannot load data from input if server is running");
        }
        this.data = parse(stream, loadFormat);
    }



    public void loadFromFile(String file, Format loadFormat) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            loadFromInputStream(stream, loadFormat);
        }
    }


    private void start() throws IOException {
        server.startServer();
        running = true;
        LOGGER.info("Mock Server started");
    }


    public void stop() throws IOException {
        server.stopServer();
        running = false;
        LOGGER.info("Mock Server stopped");
    }



    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            if ("GET".equalsIgnoreCase(method)) {
                handleGET(exchange);
            } else if ("PUT".equalsIgnoreCase(method)) {
                handlePUT(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handlePOST(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDELETE(exchange);
            }
        } catch (IllegalAccessException e) {
            sendResponse(404, null, exchange);
        } catch (IllegalArgumentException e) {
            sendResponse(400, error(e), exchange);
        } catch (Exception e) {
            sendResponse(500, error(e), exchange);
        }
    }



    private JsonNode error(Exception e) throws IOException {
        try {
            return jsonMapper.readTree(jsonMapper.writeValueAsString(
                new ServerError(e.getClass().getSimpleName(),e.getMessage(),LocalDateTime.now())
            ));
        } catch (JsonProcessingException e1) {
            throw new IOException(e1);
        }
    }


    private synchronized void handleGET(HttpExchange exchange) throws Exception {
        LOGGER.info("{} {}", "GET", exchange.getRequestURI().getPath());
        Deque<JsonNode> path = resolvePath(exchange.getRequestURI().getPath());
        JsonNode value = path.pop();
        sendResponse(200, value, exchange);
    }


    private synchronized void handlePUT(HttpExchange exchange) throws Exception {
        LOGGER.info("{} {}", "PUT", exchange.getRequestURI().getPath());
        JsonNode newValue = readRequestBody(exchange);
        assertHasID(newValue);
        Deque<JsonNode> objectPath = resolvePath(exchange.getRequestURI().getPath());
        objectPath.pop();
        JsonNode container = objectPath.pop();
        insertOrUpdateWithinContainer(container, newValue);
        sendResponse(200, newValue, exchange);
    }


    private synchronized void handleDELETE(HttpExchange exchange) throws Exception {
        LOGGER.info("{} {}", "DELETE", exchange.getRequestURI().getPath());
        String path = exchange.getRequestURI().getPath();
        String id = path.substring(path.lastIndexOf('/') + 1);
        Deque<JsonNode> objectPath = resolvePath(exchange.getRequestURI().getPath());
        objectPath.pop();
        JsonNode container = objectPath.pop();
        removeFromContainer(container, id);
        sendResponse(200, null, exchange);
    }


    private synchronized void handlePOST(HttpExchange exchange) throws Exception {
        LOGGER.info("{} {}", "POST", exchange.getRequestURI().getPath());
        JsonNode newValue = readRequestBody(exchange);
        assertHasID(newValue);
        Deque<JsonNode> objectPath = resolvePath(exchange.getRequestURI().getPath());
        JsonNode container = objectPath.pop();
        insertOrUpdateWithinContainer(container, newValue);
        sendResponse(201, newValue, exchange);
    }



    private void insertOrUpdateWithinContainer(JsonNode container, JsonNode newValue) {
        JsonNode id = assertHasID(newValue);
        if (container.isObject()) {
            ((ObjectNode)container).set(idProperty,id);
        } else if (container.isArray()) {
            removeFromContainer(container, id.asText());
            ((ArrayNode)container).add(newValue);
        }
    }



    private void removeFromContainer(JsonNode container, String id) {
        if (container.isObject()) {
            ((ObjectNode)container).remove(id);
        } else if (container.isArray()) {
            int index = indexOf(container,id);
            if (index > -1) {
                if (index > -1) {
                    ((ArrayNode)container).remove(index);
                }
            }
        }
    }




    private int indexOf(JsonNode container, String id) {
        int index = -1;
        int count = 0;
        Iterator<JsonNode> elements = container.elements();
        while (elements.hasNext()) {
            JsonNode element = elements.next();
            if (id.equals(assertHasID(element).asText())) {
                index = count;
                break;
            }
            count++;
        }
        return index;
    }




    private JsonNode assertHasID(JsonNode newValue) {
        if (!newValue.isObject()) {
            throw new IllegalArgumentException("received value is not an JSON object");
        }
        if (!newValue.has(idProperty)) {
            throw new IllegalArgumentException(
                "received value required the property: " + idProperty
            );
        }
        return newValue.get(idProperty);
    }




    private void resolveNextSegment(
        Deque<String> segments,
        Deque<JsonNode> objectPath
    ) throws IllegalAccessException {
        if (!segments.isEmpty()) {
            String segment = segments.pop();
            JsonNode container = objectPath.peek();
            if (container == null) {
                container = this.data;
            }
            JsonNode nextValue = null;
            if ("".equals(segment)) {
                nextValue = container;
            } else if (container.isObject()) {
                nextValue = container.get(segment);
            } else if (container.isArray()) {
                Iterator<JsonNode> elements = container.elements();
                while (elements.hasNext()) {
                    JsonNode child = elements.next();
                    if (child.isObject() && segment.equals(child.get("id").asText())) {
                        nextValue = child;
                        break;
                    }
                }
            }
            if (nextValue == null) {
                throw new IllegalAccessException();
            }
            objectPath.push(nextValue);
        }
    }




    public Deque<JsonNode> resolvePath(String path) throws IllegalAccessException {
        Deque<JsonNode> objectPath = new ArrayDeque<>();
        Deque<String> segments = new ArrayDeque<>(Arrays.asList(path.substring(1).split("/")));
        while (!segments.isEmpty()) {
            resolveNextSegment(segments, objectPath);
        }
        return objectPath;
    }




    private JsonNode readRequestBody(HttpExchange exchange) throws IOException {
        return jsonMapper.readTree(exchange.getRequestBody());
    }


    private void sendResponse(int status, JsonNode value, HttpExchange exchange) throws IOException {
        byte[] body = serialize(value);
        exchange.setResponseContentType(
            "application/" + format.toString().toLowerCase() + "; charset=" + charset.name()
                .toLowerCase()
        );
        exchange.setResponseCode(status);
        exchange.setResponseContentLength(body == null ? -1 : body.length);
        if (body != null) {
            String textBody = new String(body, charset);
            exchange.setResponseBody(textBody);
            LOGGER.info(textBody);
        }
        exchange.writeResponse();
    }


    public boolean isRunning() {
        return running;
    }


    public JsonNode parse(InputStream stream, Format parseFormat) throws IOException {
        if (parseFormat == Format.JSON) {
            return jsonMapper.readTree(stream);
        } else if (parseFormat == Format.XML) {
            return xmlMapper.readTree(stream);
        } else {
            throw new IllegalArgumentException("format not supported: " + parseFormat);
        }
    }


    public byte[] serialize(Object entity) throws IOException {
        if (entity == null) {
            return null;
        }
        if (format == Format.JSON) {
            return jsonMapper.writeValueAsString(entity).getBytes(charset);
        } else if (format == Format.XML) {
            String xml = xmlMapper.writeValueAsString(entity);
            String entityClassNameTagOpen = "<" + entity.getClass().getSimpleName() + ">";
            String entityClassNameTagClosed = "</" + entity.getClass().getSimpleName() + ">";
            xml = "<data>" + xml.substring(
                entityClassNameTagOpen.length(),
                xml.length() - entityClassNameTagClosed.length()
            ) + "</data>";
            return xml.getBytes(charset);
        } else {
            throw new IllegalArgumentException("format not supported: " + format);
        }
    }

}