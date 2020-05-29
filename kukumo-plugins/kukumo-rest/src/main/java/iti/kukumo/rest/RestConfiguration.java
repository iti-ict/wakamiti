/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest;




public class RestConfiguration {

    public static final String BASE_URL = "rest.baseURL";
    public static final String CONTENT_TYPE = "rest.contentType";
    public static final String FAILURE_HTTP_CODE_THRESHOLD = "rest.httpCodeThreshold";
    public static final String OAUTH2_URL = "rest.oauth2.url";
    public static final String OAUTH2_CLIENT_ID = "rest.oauth2.clientId";
    public static final String OAUTH2_CLIENT_SECRET = "rest.oauth2.clientSecret";

    public class DefaultValues {

        public static final String BASE_URL = "http://localhost:8080";
        public static final String CONTENT_TYPE = "JSON";
        public static final int FAILURE_HTTP_CODE_THRESHOLD = 500;
    }

}
