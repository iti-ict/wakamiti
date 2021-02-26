package iti.commons.gherkin;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import iti.commons.gherkin.internal.ResourceLoader;

@SuppressWarnings("unchecked")
public class GherkinDialectProvider {

    private static final Map<String, Map<String, List<String>>> DIALECTS = new HashMap<>();

    private final String defaultDialect;


    public GherkinDialectProvider(String defaultDialect) {
        this.defaultDialect = defaultDialect;
    }

    public GherkinDialectProvider() {
        this("en");
    }

    public GherkinDialect getDefaultDialect() {
        return getDialect(defaultDialect, null);
    }

    public GherkinDialect getDialect(String language, Location location) {
        Map<String, List<String>> map = DIALECTS.computeIfAbsent(language, this::readDialect);
        if (map == null) {
            throw new ParserException.NoSuchLanguageException(language, location);
        }

        return new GherkinDialect(language, map);
    }


    public GherkinDialect getDialect(Locale locale) {
        return getDialect(locale.toLanguageTag(),null);
    }



    private Map<String, List<String>> readDialect(String language) {
    	try (var reader = ResourceLoader.openReader(
			GherkinDialectProvider.class,
			"gherkin-dialect_"+language+".json"
		)) {
    		return new ObjectMapper().readValue(reader, Map.class);
		} catch (IOException e) {
			return null;
		}
    }


}
