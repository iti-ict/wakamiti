package es.iti.wakamiti.plugins.jmeter;

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
import es.iti.wakamiti.plugins.jmeter.dsl.ContentTypeUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.codehaus.plexus.util.FileUtils;
import org.mockserver.client.MockServerClient;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.MediaType;

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

    public static String read(File file) {
        try {
            return IOUtils.toString(file.toURI(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void prepare(MockServerClient client, String rootPath, Predicate<MediaType> filter) throws IOException {
        Expectation[] expectations = prepare(rootPath, filter);
        for (Expectation expectation : expectations) {
            client.when(expectation.getHttpRequest()).respond(expectation.getHttpResponse());
        }

    }

    @SuppressWarnings("unchecked")
    public static Expectation[] prepare(String rootPath, Predicate<MediaType> filter) throws IOException {
        List<Expectation> expectations = new LinkedList<>();
        Map<MediaType, Function<List<Map<String, Object>>, String>> list_to_string = Map.of(
                MediaType.APPLICATION_JSON, TestUtil::json,
                MediaType.TEXT_XML, TestUtil::xml,
                MediaType.APPLICATION_XML, TestUtil::xml
        );
        Map<MediaType, Function<String, Map<String, Object>>> string_to_map = Map.of(
                MediaType.APPLICATION_JSON, str -> TestUtil.json(str, new TypeReference<>() {
                }),
                MediaType.TEXT_XML, str -> TestUtil.xml(str, new TypeReference<>() {
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
                        MediaType mimeType = Optional.of(file)
                                .map(ContentTypeUtil::of)
                                .map(ContentType::getMimeType)
                                .map(MediaType::parse)
                                .get();

                        if (!map.containsKey(mimeType)) {
                            map.put(mimeType, new LinkedList<>());
                        }
                        if (!string_to_map.containsKey(mimeType)) {
                            throw new IllegalStateException("Mime type unexpected: " + mimeType);
                        }
                        map.get(mimeType).add(string_to_map.get(mimeType).apply(read(file)));
                    }

                    for (MediaType mimeType : map.keySet().stream().filter(filter).collect(Collectors.toList())) {
                        if (!string_to_map.containsKey(mimeType)) {
                            throw new IllegalStateException("Mime type unexpected: " + mimeType);
                        }
                        expectations.add(new Expectation(
                                        request()
                                                .withPath("/" + p)
                                                .withHeaders(
                                                        header("Content-type", ".*" + mimeType + ".*")
                                                )
                                ).thenRespond(
                                        response()
                                                .withStatusCode(HttpStatusCode.OK_200.code())
                                                .withContentType(mimeType)
                                                .withBody(list_to_string.get(mimeType).apply(map.get(mimeType)))
                                )
                        );
                    }
                });

        files.stream().map(File::toPath).filter(Files::isRegularFile)
                .map(root.toPath()::relativize)
                .forEachOrdered(p -> {
                    File file = new File(root, p.toString());
                    MediaType mimeType = Optional.of(file)
                            .map(ContentTypeUtil::of)
                            .map(ContentType::getMimeType)
                            .map(MediaType::parse)
                            .get();

                    if (!filter.test(mimeType)) return;

                    String body = read(file);
                    String path = "/" + FileUtils.removeExtension(p.toString()).replace("\\", "/");

                    expectations.add(
                            new Expectation(
                                    request()
                                            .withPath(path)
                                            .withHeaders(
                                                    header("Content-type", ".*" + mimeType + ".*")
                                            )
                            ).thenRespond(
                                    response()
                                            .withStatusCode(HttpStatusCode.OK_200.code())
                                            .withContentType(mimeType)
                                            .withBody(body)
                            )
                    );

                    if (!string_to_map.containsKey(mimeType)) {
                        throw new IllegalStateException("Mime type unexpected: " + mimeType);
                    }
                    Map<String, Object> json = string_to_map.get(mimeType).apply(body);

                    for (String key : json.keySet()) {
                        Object current = json.get(key);
                        if (current instanceof List) {
                            expectations.add(
                                    new Expectation(
                                            request()
                                                    .withPath(path + "/" + key)
                                                    .withHeaders(
                                                            header("Content-type", ".*" + mimeType + ".*")
                                                    )
                                    ).thenRespond(
                                            response()
                                                    .withStatusCode(HttpStatusCode.OK_200.code())
                                                    .withContentType(mimeType)
                                                    .withBody(list_to_string.get(mimeType).apply((List<Map<String, Object>>) current))
                                    )
                            );
                        }
                    }
                });
        expectations.add(
                new Expectation(request().withPath("/bad"))
                        .thenRespond(response().withStatusCode(HttpStatusCode.BAD_REQUEST_400.code()))
        );
        expectations.add(
                new Expectation(request().withPath("/token"))
                        .thenRespond(response().withStatusCode(HttpStatusCode.OK_200.code())
                                .withBody(read(file("data/token.json"))))
        );
        return expectations.toArray(new Expectation[0]);
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
