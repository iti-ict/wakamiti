/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RESTMessage {

    enum Method { GET, PUT, POST, DELETE, PATCH, OPTIONS }
    enum ContentType { JSON, XML, TEXT, HTML }

    private final Method method;
    private final String url;
    private final List<Object> path;
    private final Map<String,Object> params;
    private final ContentType contentType;
    private final String charset;
    private final String body;


    public static RESTMessage get(String url) {
        return new RESTMessage(Method.GET,url,new ArrayList<>(),new HashMap<>(), null, null, null);
    }

    public static RESTMessage get(String url, Map<String,Object> params) {
        return new RESTMessage(Method.GET,url,new ArrayList<>(),new HashMap<>(params), null, null, null);
    }

    public static RESTMessage get(String url, List<Object> path) {
        return new RESTMessage(Method.GET,url,new ArrayList<>(path),new HashMap<>(), null, null, null);
    }

    public static RESTMessage get(String url, List<Object> path, Map<String,Object> params) {
        return new RESTMessage(Method.GET,url,new ArrayList<>(path),new HashMap<>(params), null, null, null);
    }

    public static RESTMessage post(String url, ContentType contentType, String charset, String body) {
        return new RESTMessage(Method.GET,url,new ArrayList<>(),new HashMap<>(), contentType, charset, body);
    }

    public static RESTMessage post(String url, List<Object> path, ContentType contentType, String charset, String body) {
        return new RESTMessage(Method.GET,url,new ArrayList<>(path),new HashMap<>(), contentType, charset, body);
    }

    public static RESTMessage put(String url, ContentType contentType, String charset, String body) {
        return new RESTMessage(Method.GET,url,new ArrayList<>(),new HashMap<>(), contentType, charset, body);
    }

    public static RESTMessage put(String url, List<Object> path, ContentType contentType, String charset, String body) {
        return new RESTMessage(Method.GET,url,new ArrayList<>(path),new HashMap<>(), contentType, charset, body);
    }


    public static RESTMessage patch(String url, ContentType contentType, String charset, String body) {
        return new RESTMessage(Method.GET,url,new ArrayList<>(),new HashMap<>(), contentType, charset, body);
    }

    public static RESTMessage patch(String url, List<Object> path, ContentType contentType, String charset, String body) {
        return new RESTMessage(Method.GET,url,new ArrayList<>(path),new HashMap<>(), contentType, charset, body);
    }

    public static RESTMessage delete(String url) {
        return new RESTMessage(Method.GET,url,new ArrayList<>(),new HashMap<>(), null, null, null);
    }

    public static RESTMessage delete(String url, List<Object> path) {
        return new RESTMessage(Method.GET,url,new ArrayList<>(path),new HashMap<>(), null, null, null);
    }



    public RESTMessage(
            Method method,
            String url,
            List<Object> path,
            Map<String, Object> params,
            ContentType contentType,
            String charset,
            String body
    ) {
        this.method = method;
        this.url = url;
        this.path = path;
        this.params = params;
        this.contentType = contentType;
        this.charset = charset;
        this.body = body;
    }


    public Method method() {
        return method;
    }

    public String body() {
        return body;
    }

    public boolean hasBody() {
        return body != null && !body.isEmpty();
    }

    public String charset() {
        return charset;
    }

    public ContentType contentType() {
        return contentType;
    }

    public Map<String, Object> params() {
        return params;
    }

    public List<Object> path() {
        return path;
    }

    public String url() {
        return url;
    }
}
