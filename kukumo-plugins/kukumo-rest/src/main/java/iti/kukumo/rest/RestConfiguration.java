package iti.kukumo.rest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author ITI
 *         Created by ITI on 5/03/19
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestConfiguration {

    public static final String BASE_URL = "rest.baseURL";
    public static final String CONTENT_TYPE = "rest.contentType";
    public static final String FAILURE_HTTP_CODE_THRESHOLD = "rest.httpCodeThreshold";


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class DefaultValues {
        public static final String BASE_URL = "http://localhost:8080";
        public static final String CONTENT_TYPE = "JSON";
        public static final int FAILURE_HTTP_CODE_THRESHOLD = 500;
    }

}
