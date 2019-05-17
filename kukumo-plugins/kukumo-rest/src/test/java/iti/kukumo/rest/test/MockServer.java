package iti.kukumo.rest.test;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import groovy.util.XmlSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;


public class MockServer implements HttpHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MockServer.class);
	
    public enum Format {
        JSON, XML
    };


    protected static final class JSONError {
        private String exception;
        private String message;
        private LocalDateTime timestamp;

        public JSONError(String exception, String message, LocalDateTime timestamp) {
            this.exception = exception;
            this.message = message;
            this.timestamp = timestamp;
        }
        public String getException() {
            return exception;
        }
        public String getMessage() {
            return message;
        }
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }




    private HttpServer server;
    private boolean running = false;
    private ConcurrentHashMap<String, Object> data;
    private String idProperty;
    private Format format;
    private Charset charset;


    public MockServer (Format format, Charset charset, int port) throws IOException {
        configure(format,charset,port);
        start();
    }



    public MockServer(Format format, Charset charset, int port, String filename, Format fileFormat) throws IOException {
        try {
            configure(format, charset, port);
            loadFromFile(filename,fileFormat);
            start();
        } catch (Exception e) {
            LOGGER.error("Error starting Mock Server",e);
            stop();
            throw e;
        }
    }



    protected void configure(Format format, Charset charset, int port) throws IOException {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName("127.0.0.1"),port);
        server = HttpServer.create(address, 10);
        server.createContext("/", this);
        server.setExecutor(Executors.newSingleThreadExecutor());
        data = new ConcurrentHashMap<>();
        idProperty = "id";
        this.format = format;
        this.charset = charset;
    }



    @SuppressWarnings("unchecked")
    public void loadFromInputStream (InputStream stream, Format loadFormat) throws IOException {
        if (running) {
            throw new IllegalStateException("cannot load data from input if server is running");
        }
        Map<String,Object> data = (Map<String,Object>) parse(stream, loadFormat);
        this.data.putAll(data);
    }


    public void loadFromFile (String file, Format loadFormat) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            loadFromInputStream(stream, loadFormat);
        }
    }


    private void start() {
        server.start();
        running = true;
        LOGGER.info("Mock Server started");
    }


    public void stop() {
        server.stop(0);
        running = false;
        LOGGER.info("Mock Server stopped");
    }


    @Override
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



    private Object error(Exception e) {
        return new JSONError(
          e.getClass().getSimpleName(),
          e.getMessage(),
          LocalDateTime.now());
    }


    private synchronized void handleGET(HttpExchange exchange) throws Exception {
    		LOGGER.info("{} {}", "GET", exchange.getRequestURI().getPath());
            Deque<Object> path = resolvePath(exchange.getRequestURI().getPath());
            Object value = path.pop();

            sendResponse(200,value,exchange);
    }



    private synchronized void handlePUT(HttpExchange exchange) throws Exception {
    	LOGGER.info("{} {}", "PUT", exchange.getRequestURI().getPath());
        Object newValue = readRequestBody(exchange);
        assertHasID(newValue);
        Deque<Object> objectPath = resolvePath(exchange.getRequestURI().getPath());
        objectPath.pop();
        Object container = objectPath.pop();
        insertOrUpdateWithinContainer(container,newValue);
        sendResponse(200,newValue,exchange);
    }



    private synchronized void handleDELETE(HttpExchange exchange) throws Exception {
    	LOGGER.info("{} {}", "DELETE", exchange.getRequestURI().getPath());
        String path = exchange.getRequestURI().getPath();
        String id = path.substring(path.lastIndexOf('/')+1);
        Deque<Object> objectPath = resolvePath(exchange.getRequestURI().getPath());
        objectPath.pop();
        Object container = objectPath.pop();
        removeFromContainer(container,id);
        sendResponse(200,null,exchange);
    }




    private synchronized void handlePOST(HttpExchange exchange) throws Exception {
    	LOGGER.info("{} {}", "POST", exchange.getRequestURI().getPath());
        Object newValue = readRequestBody(exchange);
        assertHasID(newValue);
        Deque<Object> objectPath = resolvePath(exchange.getRequestURI().getPath());
        Object container = objectPath.pop();
        insertOrUpdateWithinContainer(container,newValue);
        sendResponse(201, newValue, exchange);
    }







    @SuppressWarnings("unchecked")
    private void insertOrUpdateWithinContainer(Object container, Object newValue) {
        Object id = assertHasID(newValue);
        if (container instanceof Map) {
            ((Map<String,Object>)container).put(String.valueOf(id),newValue);
        }
        else if (container instanceof List) {
            List<Object> list = (List<Object>) container;
            Optional<Object> old = list.stream().filter(o->id.equals(assertHasID(o))).findFirst();
            if (old.isPresent()) {
                list.remove(old.get());
            }
            ((List<Object>)container).add(newValue);
        }
    }




    @SuppressWarnings("unchecked")
    private void removeFromContainer(Object container, String id) {
        if (container instanceof Map) {
            ((Map<String,Object>)container).remove(id);
        }
        else if (container instanceof List) {
            List<Object> list = (List<Object>) container;
            Optional<Object> old = list.stream().filter(o->id.equals(String.valueOf(assertHasID(o)))).findFirst();
            if (old.isPresent()) {
                list.remove(old.get());
            }
        }
    }




    private Object assertHasID(Object newValue) {
        if (!(newValue instanceof Map)) {
            throw new IllegalArgumentException("received value is not an JSON object");
        }
        if (!((Map<?,?>)newValue).containsKey(idProperty)) {
            throw new IllegalArgumentException("received value required the property: "+idProperty);
        }
        return ((Map<?,?>)newValue).get(idProperty);
    }


    private void resolveNextSegment(Deque<String> segments, Deque<Object> objectPath) throws IllegalAccessException {
    	if (!segments.isEmpty()) {
            String segment = segments.pop();
            Object container = objectPath.peek();
            if (container == null) {
                container = this.data;
            }
            Object nextValue = null;
            if ("".equals(segment)) {
            	nextValue = container;
            } else if (container instanceof Map) {
                nextValue = ((Map<?,?>)container).get(segment);
            } else if (container instanceof List) {
                for (Object child : ((List<?>)container)) {
                    if ((child instanceof Map) && segment.equals(String.valueOf((((Map<?,?>)child).get("id"))))) {
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


    public Deque<Object> resolvePath(String path) throws IllegalAccessException {
        Deque<Object> objectPath = new ArrayDeque<>();
        Deque<String> segments = new ArrayDeque<>(Arrays.asList(path.substring(1).split("/")));
        while (!segments.isEmpty()) {
            resolveNextSegment(segments,objectPath);
        }
        return objectPath;
    }







    private Object readRequestBody(HttpExchange exchange) {
        return new JsonSlurper().parse(exchange.getRequestBody(), "UTF-8");
    }



    private void sendResponse (int status, Object value, HttpExchange exchange) throws IOException {
        byte[] body = serialize(value);
        exchange.getResponseHeaders().add("Content-Type", "application/"+format.toString().toLowerCase()+"; charset="+charset.name().toLowerCase());
        exchange.sendResponseHeaders(status, body == null ? -1 : body.length);
        if (body != null) {
            try (OutputStream stream = exchange.getResponseBody()) {
                stream.write(body);
            }
        }
        LOGGER.info("{}",new String(body,charset));
    }


    public boolean isRunning() {
        return running;
    }






    private Object parse(InputStream stream, Format parseFormat) throws IOException {
        if (parseFormat == Format.JSON) {
            return new JsonSlurper().parse(stream);
        }
        else if (parseFormat == Format.XML) {
            try {
				return new XmlSlurper().parse(stream);
			} catch (SAXException | ParserConfigurationException e) {
				throw new IOException(e);
			}
        } else {
            throw new IllegalArgumentException("format not supported: "+parseFormat);
        }
    }




    public byte[] serialize(Object entity) throws IOException {
        if (entity == null) {
            return null;
        }
        if (format == Format.JSON) {
            JsonBuilder json = new JsonBuilder(entity);
            return json.toString().getBytes(charset);
        } else if (format == Format.XML) {
        	XmlMapper mapper = new XmlMapper();
            String xml = mapper.writeValueAsString(entity);
            String entityClassNameTagOpen = "<" + entity.getClass().getSimpleName() + ">";
            String entityClassNameTagClosed = "</" + entity.getClass().getSimpleName() + ">";
            xml = "<data>" +
                  xml.substring(entityClassNameTagOpen.length(), xml.length() - entityClassNameTagClosed.length()) +
                  "</data>";
        	return xml.getBytes(charset);
        } else {
            throw new IllegalArgumentException("format not supported: "+format);
        }
    }

}
