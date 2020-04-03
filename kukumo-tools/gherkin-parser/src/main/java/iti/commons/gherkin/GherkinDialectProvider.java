package iti.commons.gherkin;

import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import iti.commons.gherkin.internal.ResourceLoader;

@SuppressWarnings("unchecked")
public class GherkinDialectProvider {

    private static Map<String, Map<String, List<String>>> DIALECTS;
    private final String defaultDialect;

    static {
        ObjectMapper mapper = new ObjectMapper();
        try {
            DIALECTS = mapper.readValue(
                ResourceLoader.openReader(GherkinDialectProvider.class,"gherkin-languages.json"),
                Map.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


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
        Map<String, List<String>> map = DIALECTS.get(language);
        if (map == null) {
            throw new ParserException.NoSuchLanguageException(language, location);
        }

        return new GherkinDialect(language, map);
    }


    public GherkinDialect getDialect(Locale locale) {
        return getDialect(locale.toLanguageTag(),null);
    }


    public List<String> getLanguages() {
        List<String> languages = new ArrayList<>(DIALECTS.keySet());
        sort(languages);
        return unmodifiableList(languages);
    }

}
