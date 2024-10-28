package es.iti.wakamiti.xray;

import org.slf4j.Logger;

import java.net.http.HttpClient;
import java.time.Duration;

public class XRayApi {


    private final String urlBase;
    private final String clientId;
    private final String clientSecret;
    private final String project;
    private final HttpClient httpClient;
    private final Logger logger;

    public XRayApi(String urlBase, String clientId, String clientSecret, String project, Logger logger) {
        this.urlBase = urlBase;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.project = project;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        this.logger = logger;
    }
}
