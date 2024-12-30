package es.iti.wakamiti.xray.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.xray.internal.WakamitiXRayException;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;

public class BaseApi {

    public static final String APPLICATION_JSON = "application/json";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";

    private final URL baseURL;
    private final String authorization;
    private final HttpClient httpClient;
    private final Logger logger;

    private static final ObjectMapper mapper = new ObjectMapper();

    public BaseApi(URL baseURL, String authorization, Logger logger) {
        this.baseURL = baseURL;
        this.authorization = authorization;
        this.logger = logger;

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public BaseApi(URL baseURL, String authURL, String clientId, String clientSecret, Logger logger) {
        this.logger = logger;
        this.baseURL = baseURL;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        String payload = toJSON(Map.of("client_id", clientId, "client_secret", clientSecret));
        String response = send(authRequest(authURL, payload), payload);
        this.authorization = "Bearer " + extract(response, "$");

    }


    protected <T> T extractList(String json, String path, String errorMessage) {
        Object object = validatePath(json, path, errorMessage);
        return (T) object;
    }

    protected String extract(String json, String path, String errorMessage) {
        Object object = validatePath(json, path, errorMessage);
        String extracted;
        if (object instanceof List<?>) {
            List<?> list = (List<?>) object;
            if (list.isEmpty()) {
                throw new NoSuchElementException(errorMessage);
            }
            extracted = list.get(0).toString();
        } else {
            extracted = object.toString();
        }
        logger.debug(extracted);
        return extracted;
    }

    private Object validatePath(String json, String path, String errorMessage) {
        logger.debug("checking path {}", path);
        Object object = JsonPath.read(json, path);
        if (object == null) {
            throw new NoSuchElementException(errorMessage);
        }
        return object;
    }

    protected String extract(String json, String path) {
        return extract(json, path, "Cannot extract path " + path + " from response");
    }


    protected JsonNode get(String uri) {
        try {
            return mapper.readTree(send(request("GET", uri), ""));
        } catch (JsonProcessingException e) {
            throw new WakamitiXRayException(e.getMessage());
        }
    }

    protected JsonNode post(String uri, String payload) {
        try {
            return mapper.readTree(post(uri, payload, APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            throw new WakamitiXRayException(e.getMessage());
        }
    }

    protected JsonNode put(String uri, String payload) {
        try {
            return mapper.readTree(put(uri, payload, APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            throw new WakamitiXRayException(e.getMessage());
        }
    }


    protected String patch(String uri, String payload) {
        return patch(uri, payload, APPLICATION_JSON);
    }


    protected void post(String uri, File file) {
        send(request(uri, file), "");
    }

    protected String post(String uri, String payload, String contentType) {
        return send(request("POST", uri, payload, contentType), payload);
    }

    protected String put(String uri, String payload, String contentType) {
        return send(request("PUT", uri, payload, contentType), payload);
    }


    protected String patch(String uri, String payload, String contentType) {
        return send(request("PATCH", uri, payload, contentType), payload);
    }

    private HttpRequest request(String method, String uri) {
        return HttpRequest.newBuilder()
                .method(method, HttpRequest.BodyPublishers.noBody())
                .uri(url(uri))
                .header("Authorization", authorization)
                .header("Accept", APPLICATION_JSON)
                .build();
    }

    private HttpRequest request(String method, String uri, String payload, String contentType) {
        return HttpRequest.newBuilder()
                .method(method, HttpRequest.BodyPublishers.ofString(payload))
                .uri(url(uri))
                .header("Authorization", authorization)
                .header("Content-Type", contentType)
                .header("Accept", APPLICATION_JSON)
                .build();
    }

    private HttpRequest request(String uri, File file) {
        String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().substring(0, 16);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            writeFormData(outputStream, boundary, file, Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return HttpRequest.newBuilder()
                .uri(url(uri))
                .header("Authorization", authorization)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Accept", APPLICATION_JSON)
                .header("X-Atlassian-Token", "no-check")
                .POST(HttpRequest.BodyPublishers.ofByteArray(outputStream.toByteArray()))
                .build();
    }

    private HttpRequest authRequest(String uri, String payload) {
        return HttpRequest.newBuilder()
                .method("POST", HttpRequest.BodyPublishers.ofString(payload))
                .uri(url(uri))
                .header("Content-Type", APPLICATION_JSON)
                .build();
    }

    private URI url(String uri) {
        return URI.create(baseURL + uri);
    }

    private String send(HttpRequest request, String payload) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("HTTP call => {} {} {} ", request.method(), request.uri(), payload);
            }
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (logger.isTraceEnabled()) {
                logger.trace("HTTP response => {} {}", response.statusCode(), response.body());
            }
            if (response.statusCode() >= 400) {
                throw new WakamitiException("The HTTP returned a non-OK response");
            }
            return response.body();
        } catch (IOException e) {
            throw new WakamitiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WakamitiException(e);
        }
    }

    protected String toJSON(Object value) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new WakamitiException(e);
        }
    }

    protected String valueBy(String... args) {
        List<String> criteria = new LinkedList<>();
        for (int i = 0; i < args.length; i += 2) {
            criteria.add("@." + args[i] + "=='" + args[i + 1] + "'");
        }
        return "$.value[?(" + String.join(" && ", criteria) + ")]";
    }


    private static void writeFormData(ByteArrayOutputStream outputStream, String boundary, File file, byte[] fileContent) throws IOException {
        String fileName = file.getName();

        outputStream.write(("--" + boundary + "\r\n").getBytes());
        outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n").getBytes());
        outputStream.write(("Content-Type: application/octet-stream\r\n").getBytes());
        outputStream.write(("Content-Transfer-Encoding: binary\r\n").getBytes());
        outputStream.write(("\r\n").getBytes());  // Línea en blanco

        outputStream.write(fileContent);

        outputStream.write(("\r\n").getBytes());

        outputStream.write(("--" + boundary + "--\r\n").getBytes());
    }
}
