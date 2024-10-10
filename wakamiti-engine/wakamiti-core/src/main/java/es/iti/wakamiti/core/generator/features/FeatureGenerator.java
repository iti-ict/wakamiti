/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.generator.features;

import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.core.Wakamiti;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

import static es.iti.wakamiti.api.util.JsonUtils.json;
import static org.apache.commons.lang3.StringUtils.join;

public class FeatureGenerator {

    private static final Logger LOGGER = WakamitiLogger.forClass(Wakamiti.class);

    private static final String FOLDER_SEPARATOR = "/";
    private static final String UNDERSCORE = "_";
    private static final String FEATURE_EXTENSION = ".feature";
    private static final String HTTP = "http";
    private static final String DEFAULT_PROMPT = "/generator/features/prompt.txt";

    private final HttpClient client = HttpClient.newHttpClient();

    private final OpenAIService openAIService;
    private final String apiKey;
    private final Map<String, String> apiDocs;
    private final String prompt;

    public FeatureGenerator(OpenAIService openAIService, String apiKey, String apiDocs) {
        this.openAIService = openAIService;
        this.apiKey = apiKey;
        this.apiDocs = parseApiDocs(apiDocs);
        this.prompt = loadPrompt();
    }

    public FeatureGenerator(OpenAIService openAIService, String apiKey, String apiDocs, String prompt) {
        this.openAIService = openAIService;
        this.apiKey = apiKey;
        this.apiDocs = parseApiDocs(apiDocs);
        this.prompt = prompt;
    }

    /**
     * Generates features by AI on the destination path
     *
     * @param destinationPath Destination path
     */
    public void generate(String destinationPath, String language) {
        LOGGER.info("Feature generation started...");
        try {
            Path path = Path.of(destinationPath).toAbsolutePath();
            if (!Files.exists(path) && !path.toFile().mkdirs()) {
                throw new NoSuchFileException(path.toString());
            }

            apiDocs.forEach((operationId, schema) -> {

                Path featurePath = Path.of(destinationPath, operationId + FEATURE_EXTENSION).toAbsolutePath();
                Map<String, Object> input = new LinkedHashMap<>();
                input.put("schema", schema);
                input.put("language", language);
                if (operationId.contains(FOLDER_SEPARATOR)) {
                    String[] aux = operationId.split(FOLDER_SEPARATOR);
                    input.put("apiId", aux[0]);
                    operationId = aux[1];
                }
                input.put("operationId", operationId);
                createFeature(featurePath, input);
            });
        } catch (Exception e) {
            throw new FeatureGeneratorException(e.getMessage(), e);
        }
    }


    /**
     * Parse the API docs from swagger. It can be a URL or a JSON.
     *
     * @param apiDocs API docs (file or url)
     * @return API docs JSON
     */
    private Map<String, String> parseApiDocs(String apiDocs) {
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolveFully(true);
        OpenAPIParser parser = new OpenAPIParser();

        SwaggerParseResult result = isLocation(apiDocs) ?
                parser.readLocation(apiDocs, null, parseOptions)
                : parser.readContents(apiDocs, null, parseOptions);
        if (result.getMessages() != null) result.getMessages().forEach(LOGGER::warn); // validation errors and warnings

        OpenAPI openAPI = result.getOpenAPI();
        if (openAPI == null) {
            throw new FeatureGeneratorException("Unresolved swagger schema");
        }

        Map<String, String> schemas = new LinkedHashMap<>();
        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            PathItem pathItem = entry.getValue();
            for (Map.Entry<PathItem.HttpMethod, Operation> operation : pathItem.readOperationsMap().entrySet()) {
                PathItem newPathItem = copy(pathItem);
                newPathItem.operation(operation.getKey(), operation.getValue());
                OpenAPI api = new OpenAPI();
                api.setInfo(openAPI.getInfo());
                api.setPaths(new Paths().addPathItem(entry.getKey(), newPathItem));
                schemas.put(operationId(entry.getKey(), newPathItem), json(api).toString());
            }
        }
        return schemas;
    }

    private PathItem copy(PathItem item) {
        return new PathItem()
                .description(item.getDescription())
                .summary(item.getSummary())
                .servers(item.getServers())
                .$ref(item.get$ref())
                .extensions(item.getExtensions())
                .parameters(item.getParameters());
    }

    private boolean isLocation(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String operationId(String endpoint, PathItem path) {
        Map<PathItem.HttpMethod, Operation> operation = path.readOperationsMap();
        return operation.keySet().stream().findFirst()
                .map(op -> {
                    String opId = op.toString().toLowerCase() + endpointFormat(endpoint);
                    return Optional.ofNullable(operation.get(op).getTags())
                            .filter(tags -> !tags.isEmpty())
                            .map(tags -> tags.get(0).replaceAll("\\s+", "-"))
                            .map(t -> t + "/" + opId)
                            .orElse(opId);
                })
                .orElseThrow(() -> new FeatureGeneratorException("Cannot generate id of operation [{}]", endpoint));
    }

    private String endpointFormat(String endpoint) {
        StringBuilder builder = new StringBuilder();
        List<String> parameters = new LinkedList<>();
        for (String it : endpoint.replaceAll("[\\s*!;,?:@&=+$.~'()]", "").split(FOLDER_SEPARATOR)) {
            if (Strings.isBlank(it)) continue;
            if (it.startsWith("{")) {
                it = it.replaceAll("\\{(.+?)}", "$1");
                parameters.add(it.substring(0, 1).toUpperCase() + it.substring(1));
            } else {
                builder.append(it.substring(0, 1).toUpperCase()).append(it.substring(1));
            }
        }
        if (!parameters.isEmpty()) {
            builder.append("By").append(join(parameters, "And"));
        }
        return builder.toString();
    }

    /**
     * Loads the default prompt from a resource file
     *
     * @return The loaded prompt
     */
    private String loadPrompt() {
        try (InputStream in = Objects.requireNonNull(getClass().getResourceAsStream(DEFAULT_PROMPT))) {
            byte[] data = IOUtils.toByteArray(in);
            return new String(data);
        } catch (IOException e) {
            throw new FeatureGeneratorException(e.getMessage(), e);
        }
    }

    /**
     * Creates the feature file with the AI response text
     *
     * @param featurePath Path where the features have to be created
     * @param input       Swagger's endpoint info
     */
    private void createFeature(Path featurePath, Map<String, Object> input) {
        if (!Files.exists(featurePath) || featurePath.toFile().delete()) {
            try {
                if (!Files.exists(featurePath.getParent()) && !featurePath.getParent().toFile().mkdirs()) {
                    throw new NoSuchFileException(featurePath.getParent().toString(), null, "Cannot create dir");
                }
                Path path = Files.createFile(featurePath);
                String content = openAIService.runPrompt(prompt.concat(join(input.entrySet(), System.lineSeparator())), apiKey);
                Files.write(path, content.getBytes());
                LOGGER.info("File '{}' created", featurePath);
            } catch (IOException | URISyntaxException e) {
                throw new FeatureGeneratorException("Cannot create feature [{}]", featurePath, e);
            }
        }
    }
}
