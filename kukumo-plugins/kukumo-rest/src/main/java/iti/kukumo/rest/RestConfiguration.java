/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;



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
