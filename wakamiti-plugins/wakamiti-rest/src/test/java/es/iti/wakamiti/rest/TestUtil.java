package es.iti.wakamiti.rest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.FileUtils;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.MediaType;
import org.mockserver.model.RegexBody;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.RegexBody.regex;

public class TestUtil {

    private static final XmlMapper xmlMapper = XmlMapper.builder().defaultUseWrapper(false)
            .configure(ToXmlGenerator.Feature.UNWRAP_ROOT_OBJECT_NODE, true)
            .build();

    public static String json(Map<?, ?> map) {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String json(List<?> list) {
        try {
            return new ObjectMapper().writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T json(String string, TypeReference<T> type) {
        try {
            return new ObjectMapper().readValue(string, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String xml(Map<?, ?> map) {
        try {
            return xmlMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String xml(List<?> list) {
        try {
            return xmlMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T xml(String string, TypeReference<T> type) {
        try {
            SimpleModule m = new SimpleModule("module", new Version(1, 0, 0, null, null, null));
            m.addDeserializer(Map.class, new CustomDeserializer());
            xmlMapper.registerModule(m);
            return xmlMapper.readValue(string, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <K, V> Map<K, V> map(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
        var map = new LinkedHashMap<K, V>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        return map;
    }

    public static <K, V> Map<K, V> map(K key1, V value1, K key2, V value2, K key3, V value3) {
        var map = new LinkedHashMap<K, V>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    public static <K, V> Map<K, V> map(K key1, V value1, K key2, V value2) {
        var map = new LinkedHashMap<K, V>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    public static <K, V> Map<K, V> map(K key1, V value1) {
        var map = new LinkedHashMap<K, V>();
        map.put(key1, value1);
        return map;
    }

    public static File file(String path) {
        try {
            return new File(TestUtil.class.getClassLoader().getResource(path).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static String file(String subtype, String contentType, String name, String content) {
        return "Content-Disposition: " + subtype + "; name=\"" + name + "\".+" +
                "Content-Type: " + contentType + ".+" +
                content.replaceAll("([{}\\[\\]()])", "\\\\$1");
    }

    public static RegexBody attached(String... files) {
        String boundary = "--" + RestAssured.config().getMultiPartConfig().defaultBoundary();
        return regex(".*" + boundary + ".*" + String.join(".*" + boundary + ".*", files) + ".*" + boundary + ".*");
    }

    public static String read(File file) {
        try {
            return IOUtils.toString(file.toURI(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void prepare(MockServerClient client, String rootPath, Predicate<MediaType> filter) throws IOException {
        Map<MediaType, Function<List<Map<String, Object>>, String>> list_to_string = Map.of(
                MediaType.APPLICATION_JSON, TestUtil::json,
                MediaType.APPLICATION_XML, TestUtil::xml
        );
        Map<MediaType, Function<String, Map<String, Object>>> string_to_map = Map.of(
                MediaType.APPLICATION_JSON, str -> TestUtil.json(str, new TypeReference<>() {
                }),
                MediaType.APPLICATION_XML, str -> TestUtil.xml(str, new TypeReference<>() {
                })
        );

        File root = file(rootPath);
        List<File> files = listFiles(root);

        files.stream().map(File::toPath).filter(Files::isDirectory)
                .map(root.toPath()::relativize)
                .forEachOrdered(p -> {
                    Map<MediaType, List<Map<String, Object>>> map = new HashMap<>();

                    File[] fs = new File(root, p.toString()).listFiles();
                    fs = fs == null ? new File[0] : fs;
                    Arrays.sort(fs);
                    for (File file : fs) {
                        MediaType mimeType = Optional.of(file.getName())
                                .map(FileUtils::getExtension)
                                .map(ContentTypeHelper.contentTypeFromExtension::get)
                                .map(ContentType::toString)
                                .map(MediaType::parse)
                                .get();

                        if (!map.containsKey(mimeType)) {
                            map.put(mimeType, new LinkedList<>());
                        }
                        map.get(mimeType).add(string_to_map.get(mimeType).apply(read(file)));
                    }

                    for (MediaType mimeType : map.keySet().stream().filter(filter).collect(Collectors.toList())) {
                        client.when(request()
                                        .withPath("/" + p)
                                        .withHeaders(
                                                header("Content-type", ".*" + mimeType + ".*")
                                        )
                                )
                                .respond(response()
                                        .withStatusCode(HttpStatusCode.OK_200.code())
                                        .withContentType(mimeType)
                                        .withBody(list_to_string.get(mimeType).apply(map.get(mimeType)))
                                );
                    }
                });

        files.stream().map(File::toPath).filter(Files::isRegularFile)
                .map(root.toPath()::relativize)
                .forEachOrdered(p -> {
                    File file = new File(root, p.toString());
                    MediaType mimeType = Optional.of(file.getName())
                            .map(FileUtils::getExtension)
                            .map(ContentTypeHelper.contentTypeFromExtension::get)
                            .map(ContentType::toString)
                            .map(MediaType::parse)
                            .get();

                    if (!filter.test(mimeType)) return;

                    String body = read(file);
                    String path = "/" + FileUtils.removeExtension(p.toString()).replace("\\", "/");

                    client.when(request()
                                    .withPath(path)
                                    .withHeaders(
                                            header("Content-type", ".*" + mimeType + ".*")
                                    )
                            )
                            .respond(response()
                                    .withStatusCode(HttpStatusCode.OK_200.code())
                                    .withContentType(mimeType)
                                    .withBody(body)
                            );

                    Map<String, Object> json = string_to_map.get(mimeType).apply(body);

                    for (String key : json.keySet()) {
                        Object current = json.get(key);
                        if (current instanceof List) {
                            client.when(request()
                                            .withPath(path + "/" + key)
                                            .withHeaders(
                                                    header("Content-type", ".*" + mimeType + ".*")
                                            )
                                    )
                                    .respond(response()
                                            .withStatusCode(HttpStatusCode.OK_200.code())
                                            .withContentType(mimeType)
                                            .withBody(list_to_string.get(mimeType).apply((List<Map<String, Object>>) current))
                                    );
                        }
                    }
                });

        client.when(request().withPath("/bad"))
                .respond(response().withStatusCode(HttpStatusCode.BAD_REQUEST_400.code()));
    }

    private static List<File> listFiles(final File folder) throws IOException {
        List<File> files = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(folder.getPath()))) {
            paths.filter(Files::isRegularFile).map(Path::toFile).forEach(files::add);
        }
        new ArrayList<>(files).stream().map(File::getParentFile).distinct().forEach(files::add);
        files.remove(folder);
        return files;
    }

    static class CustomDeserializer extends JsonDeserializer<Map<String, ?>> {

        @Override
        public Map<String, ?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            Map<String, Object> map = new LinkedHashMap<>();
            JsonToken token;
            while ((token = parser.nextToken()) != null && token != JsonToken.END_OBJECT) {

                if (token == JsonToken.FIELD_NAME) {
                    String name = parser.getCurrentName();
                    token = parser.nextToken();

                    if (token == JsonToken.VALUE_STRING) {
                        map.put(name, parser.readValueAs(String.class));
                    }
                    if (token == JsonToken.START_OBJECT) {
                        String field = parser.nextFieldName();

                        if (field != null && field.endsWith("List")) {
                            List<Object> list = new LinkedList<>();

                            if (parser.nextToken() == JsonToken.START_OBJECT) {
                                while (parser.nextToken() != JsonToken.END_OBJECT) {
                                    list.add(deserialize(parser, context));
                                }
                            }
                            map.put(name, list);
                        } else {
                            map.put(name, deserialize(parser, context));
                        }
                    }
                    if (token == JsonToken.END_OBJECT) {
                        break;
                    }
                }
            }
            return map;
        }
    }

}
