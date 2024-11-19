package es.iti.wakamiti.xray.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import es.iti.wakamiti.api.WakamitiException;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class BaseApi {

    public static final String APPLICATION_JSON = "application/json";

    private final URL baseURL;
    private final String authorization;
    private final HttpClient httpClient;
    private final Logger logger;

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


    protected String get(String uri) {
        return send(request("GET", uri), "");
    }


    protected String post(String uri, String payload) {
        return post(uri, payload, APPLICATION_JSON);
    }

    protected String put(String uri, String payload) {
        return put(uri, payload, APPLICATION_JSON);
    }


    protected String patch(String uri, String payload) {
        return patch(uri, payload, APPLICATION_JSON);
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
}
