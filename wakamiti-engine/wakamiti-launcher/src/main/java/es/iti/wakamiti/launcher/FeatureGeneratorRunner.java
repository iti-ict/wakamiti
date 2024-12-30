package es.iti.wakamiti.launcher;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.api.util.ThrowableSupplier;
import es.iti.wakamiti.core.generator.features.FeatureGenerator;
import es.iti.wakamiti.core.generator.features.FeatureGeneratorException;
import es.iti.wakamiti.core.generator.features.OpenAIService;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;


public class FeatureGeneratorRunner {

    private final CliArguments arguments;
    private final OpenAIService openAIService;

    public FeatureGeneratorRunner(CliArguments arguments, OpenAIService openAIService) {
        this.arguments = arguments;
        this.openAIService = openAIService;
    }

    public void run() {
        String apiDocs = getValueString(CliArguments.ARG_API_DOCS);
        String token = getValueString(CliArguments.ARG_AI_TOKEN);
        String destinationPath = Optional.ofNullable(getValueString(CliArguments.ARG_FEATURE_GENERATION_PATH))
                .orElse(".");
        String language = Optional.ofNullable(getValueString(CliArguments.ARG_LANGUAGE))
                .orElseGet((ThrowableSupplier<String>) () -> arguments.wakamitiConfiguration()
                        .get(WakamitiConfiguration.LANGUAGE, String.class).orElse("en"));

        FeatureGenerator featureGenerator = new FeatureGenerator(openAIService, token, apiDocs);
        featureGenerator.generate(destinationPath, language);
    }

    private String getValueString(String key) {
        String value = arguments.getValue(key);
        if (StringUtils.isBlank(value)) {
            throw new FeatureGeneratorException("Argument '{}' not found.", key);
        }
        return value;
    }

}
