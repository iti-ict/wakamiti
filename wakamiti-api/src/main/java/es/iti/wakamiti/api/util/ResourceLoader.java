/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import es.iti.wakamiti.api.Resource;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.PropertyEvaluator;
import es.iti.wakamiti.api.extensions.ResourceType;
import imconfig.ConfigurationFactory;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * A utility class for loading and working with resources.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class ResourceLoader {

    private static final String CLASSPATH_PROTOCOL = "classpath:";
    private static final String FILE_PROTOCOL = "file";

    private static final Logger LOGGER = WakamitiLogger.forClass(ResourceLoader.class);
    private static final int BUFFER_SIZE = 2048;
    private final Charset charset;
    private File workingDir = new File(".");

    public ResourceLoader(Charset charset) {
        this.charset = charset;
        Locale.setDefault(Locale.ENGLISH); // avoid different behaviors regarding the OS language
    }

    public ResourceLoader() {
        this(StandardCharsets.UTF_8);
    }

    /**
     * Sets the working directory for resource loading.
     *
     * @param workingDir The working directory.
     */
    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir.getAbsoluteFile();
    }

    /**
     * Sets the working directory for resource loading.
     *
     * @param workingDir The working directory.
     */
    public void setWorkingDir(Path workingDir) {
        this.workingDir = workingDir.toAbsolutePath().toFile();
    }

    /**
     * Creates a resource from an input stream using the specified resource type.
     *
     * @param <T>          The type of the resource.
     * @param resourceType The resource type.
     * @param inputStream  The input stream.
     * @return A resource instance.
     * @throws WakamitiException If an error occurs while reading the input stream.
     */
    public <T> Resource<T> fromInputStream(ResourceType<T> resourceType, InputStream inputStream) {
        try {
            return new Resource<>("", "", resourceType.parse(inputStream, charset));
        } catch (IOException e) {
            throw new WakamitiException("Error reading input stream: ", e);
        }
    }

    /**
     * Creates a new {@code Reader} based on the provided {@code URL}.
     * <p>
     * The obtained reader is not automatically managed and should be
     * closed manually after using it.
     * </p>
     *
     * @param url The URL from which to read the content.
     * @return A new {@code Reader} for the content of the specified URL.
     * @throws IOException              If an I/O error occurs while reading the
     *                                  content from the URL.
     * @throws CharacterCodingException If there is an error checking the
     *                                  charset during decoding.
     * @see CharsetDecoder
     * @see StandardCharsets
     * @see #charset
     */
    public Reader reader(URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            byte[] bytes = toByteArray(inputStream);
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer resourceBuffer = decoder.decode(ByteBuffer.wrap(bytes));
            return new CharArrayReader(resourceBuffer.array());
        } catch (CharacterCodingException e) {
            LOGGER.error(
                    "ERROR CHECKING CHARSET {} IN RESOURCE {uri} : {error}",
                    charset,
                    url
            );
            throw e;
        }
    }

    /**
     * Reads the content of the file as a string using {@code UTF_8}
     * encoding and evaluates global properties.
     *
     * @param file The file to read
     * @return The file content
     */
    public String readFileAsString(File file) {
        return readFileAsString(file, StandardCharsets.UTF_8);
    }

    /**
     * Reads the content of the file as a string using the specified
     * {@link Charset} and evaluates global properties.
     *
     * @param file    The file to read.
     * @param charset The file charset.
     * @return The file content.
     */
    public String readFileAsString(File file, Charset charset) {
        try (FileInputStream inputStream = new FileInputStream(absolutePath(file))) {
            return PropertyEvaluator.makeEvalIfCan(toString(inputStream, charset)).value();
        } catch (IOException e) {
            throw new WakamitiException("Error reading text file {} : {}", file, e.getMessage(), e);
        }
    }

    /**
     * Creates a new {@code Reader} based on the provided path.
     * <p>
     * This method creates a {@code Reader} for the content specified by
     * the given path. The behavior depends on the format of the path:
     * <ul>
     * <li>If the path starts with {@code "classpath:"}, it attempts to
     * locate the resource in the classpath using the current thread's
     * context class loader.</li>
     * <li>If the path starts with {@code "http:"}, it tries to download
     * the resource from the web.</li>
     * <li>If the path starts with {@code "file:"}, it attempts to locate
     * the resource in the file system using the absolute path provided in
     * the URI.</li>
     * <li>Otherwise, it tries to locate the resource in the file system
     * from the application directory.</li>
     * </ul>
     * The obtained reader is not automatically managed and should be closed
     * manually after using it.
     * </p>
     *
     * @param path The path specifying the location of the resource.
     * @return A new {@code Reader} for the content specified by the given
     * path.
     * @throws IOException              If an I/O error occurs while creating the reader
     *                                  or reading the content.
     * @throws CharacterCodingException If there is an error checking the
     *                                  charset during decoding.
     * @see #reader(URL)
     * @see #charset
     */
    public Reader reader(String path) throws IOException {
        if (path.startsWith(CLASSPATH_PROTOCOL)) {
            URL url = Thread.currentThread().getContextClassLoader()
                    .getResource(path.replace(CLASSPATH_PROTOCOL, ""));
            return reader(url);
        } else {
            return reader(URI.create(path).toURL());
        }
    }

    /**
     * Obtains a {@code ResourceBundle} based on the specified bundle name
     * and locale.
     * <p>
     * This method retrieves a {@code ResourceBundle} using the provided bundle name
     * and locale. It differs from {@link ResourceBundle#getBundle(String, Locale)}
     * in two aspects:
     * <ul>
     * <li>The content is read using the charset defined in the {@code ResourceLoader}
     * instance.</li>
     * <li>If there is more than one resource available (e.g., a plugin redefines an
     * existing property file), the resource bundle will contain the composition of
     * values.</li>
     * </ul>
     * </p>
     *
     * @param resourceBundle The base name of the resource bundle, a fully qualified
     *                       class name.
     * @param locale         The locale for which a resource bundle is desired.
     * @return A {@code ResourceBundle} for the specified bundle name and locale.
     * @see ResourceBundle#getBundle(String, Locale, ClassLoader)
     * @see #charset
     */
    public ResourceBundle resourceBundle(String resourceBundle, Locale locale) {
        return ResourceBundle.getBundle(resourceBundle, locale,
                Thread.currentThread().getContextClassLoader());
    }

    /**
     * Reads the content of a resource specified by the given path as a string.
     * <p>
     * This method discovers the resource using the provided path, reads its content,
     * and returns it as a string. If there are multiple resources matching the path,
     * the content of the first discovered resource is returned.
     * </p>
     *
     * @param path The path to the resource. The path can include placeholders and
     *             may start with protocols like {@code classpath:}, {@code http:},
     *             {@code file:}, or a relative path.
     * @return The content of the discovered resource as a string.
     * @throws WakamitiException If an error occurs while reading the text file.
     * @see #discoverResources(List, Predicate, Parser)
     * @see #toString(InputStream, Charset)
     */
    public String readResourceAsString(String path) {
        return discoverResources(Collections.singletonList(path), x -> true, this::toString).get(0)
                .content().toString();
    }

    /**
     * Discovers and parses resources of a specific type from the given list of paths.
     * <p>
     * This method discovers resources from the specified paths, filters them based on the
     * provided {@link ResourceType}, and parses each resource using the provided
     * {@code resourceType}. It returns a list of {@link Resource} objects containing the
     * parsed content.
     * </p>
     *
     * @param paths        The list of paths to discover resources from.
     * @param resourceType The {@link ResourceType} used to filter and parse resources.
     * @param <T>          The type of the resource content.
     * @return A list of {@link Resource} objects containing the parsed content of the
     * discovered resources.
     * @see #discoverResources(List, Predicate, Parser)
     * @see ResourceType#acceptsFilename(String)
     * @see ResourceType#parse(InputStream, Charset)
     */
    public <T> List<Resource<?>> discoverResources(
            List<String> paths,
            ResourceType<T> resourceType
    ) {
        if (LOGGER.isInfoEnabled()) {
            List<Path> absolutePaths = paths.stream().map(Paths::get).map(Path::toAbsolutePath)
                    .collect(Collectors.toList());
            LOGGER.info(
                    "Discovering resources of type {resourceType} in paths: {uri} ...",
                    resourceType.extensionMetadata().name(),
                    absolutePaths
            );
        }
        return discoverResources(paths, resourceType::acceptsFilename, resourceType::parse);
    }

    /**
     * Discovers and parses resources of a specific type from the given path.
     * <p>
     * This method discovers resources from the specified path, filters them based on the
     * provided {@link ResourceType}, and parses each resource using the provided
     * {@code resourceType}. It returns a list of {@link Resource} objects containing the
     * parsed content.
     * </p>
     *
     * @param path         The path to discover resources from.
     * @param resourceType The {@link ResourceType} used to filter and parse resources.
     * @param <T>          The type of the resource content.
     * @return A list of {@link Resource} objects containing the parsed content of the
     * discovered resources.
     * @see #discoverResources(String, Predicate, Parser)
     * @see ResourceType#acceptsFilename(String)
     * @see ResourceType#parse(InputStream, Charset)
     */
    public <T> List<Resource<?>> discoverResources(String path, ResourceType<T> resourceType) {
        return discoverResources(path, resourceType::acceptsFilename, resourceType::parse);
    }

    /**
     * Discovers and parses resources of a specific type from multiple paths.
     * <p>
     * This method iterates over a list of paths, discovers resources from each
     * path, filters them based on the provided {@code filenameFilter}, and
     * parses each resource using the provided {@code parser}. It returns a list
     * of {@link Resource} objects containing the parsed content.
     * </p>
     *
     * @param paths          The list of paths to discover resources from.
     * @param filenameFilter The {@link Predicate} used to filter resources
     *                       based on their filenames.
     * @param parser         The {@link Parser} used to parse the content of each
     *                       resource.
     * @param <T>            The type of the resource content.
     * @return A list of {@link Resource} objects containing the parsed content of
     * the discovered resources.
     * @see #discoverResources(String, Predicate, Parser)
     * @see ResourceType#acceptsFilename(String)
     * @see ResourceType#parse(InputStream, Charset)
     */
    public <T> List<Resource<?>> discoverResources(
            List<String> paths,
            Predicate<String> filenameFilter,
            Parser<T> parser
    ) {
        List<Resource<?>> discovered = new ArrayList<>();
        for (String path : paths) {
            discovered.addAll(discoverResources(path, filenameFilter, parser));
        }
        if (LOGGER.isInfoEnabled()) {
            discovered.forEach(
                    resource -> LOGGER.info(
                            "Discovered resource {uri}",
                            resource.absolutePath()
                    )
            );
        }
        return discovered;
    }

    /**
     * Discovers and parses resources of a specific type from a given path.
     * <p>
     * This method discovers resources from a specified path, filters them
     * based on the provided {@code filenameFilter}, and parses each resource
     * using the provided {@code parser}. It returns a list of {@link Resource}
     * objects containing the parsed content.
     * </p>
     * <p>
     * The path can be either a classpath resource or a file system resource.
     * If the path starts with the {@code classpath:} protocol, it is treated as
     * a classpath resource. Otherwise, it is considered a file system resource.
     * In the case of classpath resources, multiple resources may be discovered
     * if there are multiple occurrences in the classpath.
     * </p>
     *
     * @param path           The path from which to discover resources.
     * @param filenameFilter The {@link Predicate} used to filter resources based
     *                       on their filenames.
     * @param parser         The {@link Parser} used to parse the content of each
     *                       resource.
     * @param <T>            The type of the resource content.
     * @return A list of {@link Resource} objects containing the parsed content
     * of the discovered resources.
     * @see #discoverResources(List, Predicate, Parser)
     * @see #loadFromClasspath(String, ClassLoader)
     * @see #discoverResourcesInURI(String, URI, Predicate, Parser, List)
     */
    public <T> List<Resource<?>> discoverResources(
            String path,
            Predicate<String> filenameFilter,
            Parser<T> parser
    ) {
        List<Resource<?>> discovered = new ArrayList<>();
        try {
            if (path.startsWith(CLASSPATH_PROTOCOL)) {
                String classPath = path.replace(CLASSPATH_PROTOCOL, "");
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                String absoluteClassPath = new File(classLoaderFolder(classLoader) + classPath).getPath();
                for (URI uri : loadFromClasspath(classPath, classLoader)) {
                    discoverResourcesInURI(
                            absoluteClassPath,
                            uri,
                            filenameFilter,
                            parser,
                            discovered
                    );
                }
            } else {
                path = Path.of(path).normalize().toString();
                if (path.endsWith("/") || path.endsWith("\\")) {
                    path = path.substring(0, path.length() - 1);
                }
                URI uri;
                if (Paths.get(path).isAbsolute()) {
                    uri = Paths.get(path).toUri();
                } else {
                    uri = new File(workingDir, path).toURI();
                }
                discoverResourcesInURI(path, uri, filenameFilter, parser, discovered);
            }
        } catch (IOException e) {
            LOGGER.debug("Error discovering resource: {}", e.getMessage(), e);
        }
        return discovered;
    }

    /**
     * Discovers resources from a specified URI, applying filtering and parsing.
     * <p>
     * This method is called internally by {@link #discoverResources(String, Predicate, Parser)}
     * to discover resources from a specified URI. It applies filtering based on
     * the provided {@code filenameFilter} and parses each resource using the
     * provided {@code parser}. It adds the parsed resources to the {@code discovered}
     * list.
     * </p>
     * <p>
     * Depending on the URI scheme, this method handles the discovery differently:
     * <li>If the URI scheme is {@code File} it invokes
     * {@link #discoverResourcesInFile(String, File, Predicate, Parser, List)} to
     * discover file system resources.</li>
     * <li>If the URI scheme is not {@code File} it adds the resource directly to
     * the {@code discovered} list after parsing it from the URL stream.</li>
     * </p>
     *
     * @param startPath      The starting path used in relative resource paths.
     * @param uri            The URI of the resource.
     * @param filenameFilter The {@link Predicate} used to filter resources based
     *                       on their filenames.
     * @param parser         The {@link Parser} used to parse the content of each
     *                       resource.
     * @param discovered     The list where the discovered resources will be added.
     * @param <T>            The type of the resource content.
     * @see #discoverResources(String, Predicate, Parser)
     * @see #discoverResourcesInFile(String, File, Predicate, Parser, List)
     */
    protected <T> void discoverResourcesInURI(
            String startPath,
            URI uri,
            Predicate<String> filenameFilter,
            Parser<T> parser,
            List<Resource<?>> discovered
    ) {
        if (FILE_PROTOCOL.equals(uri.getScheme())) {
            discoverResourcesInFile(
                    startPath,
                    new File(uri),
                    filenameFilter,
                    parser,
                    discovered
            );
        } else {
            try {
                discovered.add(
                        new Resource<>(
                                uri.toString(), uri.toString(), parser.parse(uri.toURL().openStream(), charset)
                        )
                );
            } catch (IOException e) {
                LOGGER.debug("{!error} Error discovering resource: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Recursively discovers resources from a specified file, applying filtering
     * and parsing.
     * <p>
     * This method is called internally by {@link #discoverResourcesInURI(String, URI, Predicate, Parser, List)}
     * to discover resources from a specified file. It applies filtering based on
     * the provided {@code filenameFilter} and parses each resource using the
     * provided {@code parser}. It adds the parsed resources to the {@code discovered}
     * list. The method recursively explores subdirectories if the current file is
     * a directory.
     * </p>
     * <p>
     * Resources are discovered based on filename filtering. If a file passes the filter, its content is parsed and added
     * to the list of discovered resources.
     * </p>
     *
     * @param startPath      The starting path used in relative resource paths.
     * @param file           The file from which to discover resources.
     * @param filenameFilter The {@link Predicate} used to filter resources based
     *                       on their filenames.
     * @param parser         The {@link Parser} used to parse the content of each
     *                       resource.
     * @param discovered     The list where the discovered resources will be added.
     * @param <T>            The type of the resource content.
     * @see #discoverResourcesInURI(String, URI, Predicate, Parser, List)
     */
    protected <T> void discoverResourcesInFile(
            String startPath,
            File file,
            Predicate<String> filenameFilter,
            Parser<T> parser,
            List<Resource<?>> discovered
    ) {
        if (file.isDirectory() && file.listFiles() != null) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                discoverResourcesInFile(startPath, child, filenameFilter, parser, discovered);
            }
        } else if (filenameFilter.test(file.getName())) {
            try (InputStream stream = new FileInputStream(file)) {
                discovered.add(
                        new Resource<>(
                                "file:" + file.getAbsolutePath(),
                                relative(startPath, file.getAbsolutePath()),
                                parser.parse(stream, charset)
                        )
                );
            } catch (IOException e) {
                LOGGER.error(e.toString(), e);
            }
        }

    }

    /**
     * Computes the relative path from the given starting path to the specified
     * absolute path.
     * <p>
     * This method calculates the relative path from the {@code startPath} to
     * the {@code absolutePath}. It handles different scenarios, including cases
     * where the absolute path ends with or contains the start path. The resulting
     * relative path is returned.
     * </p>
     *
     * @param startPath    The starting path.
     * @param absolutePath The absolute path to which the relative path is
     *                     calculated.
     * @return The relative path from the starting path to the absolute path.
     */
    private String relative(String startPath, String absolutePath) {
        if (absolutePath.endsWith(startPath)) {
            return startPath;
        } else if (absolutePath.contains(startPath)) {
            String partialPath = absolutePath.substring(absolutePath.indexOf(startPath));
            return partialPath.substring(startPath.length() + 1);
        } else {
            String partialPath = absolutePath.substring(startPath.length() - 1);
            return partialPath.substring(startPath.length() + 1);
        }
    }

    /**
     * Returns the absolute path for the given file. If the file's path is
     * already absolute, the original file is returned. Otherwise, the absolute
     * path is constructed based on the working directory of the resource loader.
     *
     * @param file The file for which to obtain the absolute path.
     * @return The absolute path of the file.
     */
    public File absolutePath(File file) {
        if (file.isAbsolute()) {
            return file;
        }
        return new File(workingDir, file.toString());
    }

    /**
     * Returns the absolute path for the given path. If the path is already
     * absolute, the original path is returned. Otherwise, the absolute path
     * is constructed based on the working directory of the resource loader.
     *
     * @param path The path for which to obtain the absolute path.
     * @return The absolute path of the given path.
     */
    public Path absolutePath(Path path) {
        if (path.isAbsolute()) {
            return path;
        }
        return workingDir.toPath().resolve(path);
    }

    /**
     * Loads resources from the classpath with the specified classpath prefix.
     * Resources are identified by their path relative to the given classpath prefix.
     * The method returns a set of URIs corresponding to the located resources.
     *
     * @param classPath   The classpath prefix for which to locate resources.
     * @param classLoader The ClassLoader to use for resource loading.
     * @return A Set of URIs representing the located resources in the classpath.
     * @throws IOException If an I/O error occurs while loading resources.
     */
    protected Set<URI> loadFromClasspath(String classPath, ClassLoader classLoader) {
        try {
            return Collections.list(classLoader.getResources(classPath)).stream()
                    .map(URL::toString)
                    .filter(it -> !it.endsWith("/"))
                    .map(URI::create)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            LOGGER.error("{error}", e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    private String classLoaderFolder(ClassLoader classLoader) throws IOException {
        try {
            return classLoader.getResource(".").toURI().getPath();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        try (var outputStream = new ByteArrayOutputStream()) {
            transfer(inputStream, outputStream, new byte[BUFFER_SIZE]);
            return outputStream.toByteArray();
        }
    }

    private String toString(InputStream inputStream, Charset stringCharset) throws IOException {
        return new String(toByteArray(inputStream), stringCharset);
    }

    private void transfer(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        int n;
        while ((n = input.read(buffer)) > 0) {
            output.write(buffer, 0, n);
        }
    }

    /**
     * A functional interface for parsing resource content.
     *
     * @param <T> The type of the parsed content.
     */
    public interface Parser<T> {
        T parse(InputStream stream, Charset charset) throws IOException;
    }

    public static Map<String, ContentType> contentTypeFromExtension = ConfigurationFactory.instance()
            .fromResource("mime-types.properties", ResourceLoader.class.getClassLoader())
            .asMap().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> ContentType.create(e.getValue())));

    public static ContentType getContentType(File file) {
        return Optional.of(file.getName())
                .map(FileUtils::getExtension)
                .map(ResourceLoader.contentTypeFromExtension::get)
                .orElse(ContentType.DEFAULT_BINARY);
    }

}