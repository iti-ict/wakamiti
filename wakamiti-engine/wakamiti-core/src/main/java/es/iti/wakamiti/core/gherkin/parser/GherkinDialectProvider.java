/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.iti.wakamiti.core.gherkin.parser.internal.ResourceLoader;


@SuppressWarnings("unchecked")
public class GherkinDialectProvider {

    private static final Map<String, Map<String, List<String>>> DIALECTS = new HashMap<>();

    private final String defaultDialect;

    /**
     * Creates a provider with a caller-selected fallback dialect.
     *
     * @param defaultDialect language code used when a document has no language
     *                       directive
     */
    public GherkinDialectProvider(
            String defaultDialect
    ) {
        this.defaultDialect = defaultDialect;
    }

    public GherkinDialectProvider() {
        this("en");
    }

    public es.iti.wakamiti.core.gherkin.parser.GherkinDialect getDefaultDialect() {
        return getDialect(defaultDialect, null);
    }

    public es.iti.wakamiti.core.gherkin.parser.GherkinDialect getDialect(
            String language,
            Location location
    ) {
        Map<String, List<String>> map = DIALECTS.computeIfAbsent(language, this::readDialect);
        if (map == null) {
            throw new ParserException.NoSuchLanguageException(language, location);
        }

        return new es.iti.wakamiti.core.gherkin.parser.GherkinDialect(language, map);
    }

    public GherkinDialect getDialect(
            Locale locale
    ) {
        return getDialect(locale.toLanguageTag(), null);
    }

    private Map<String, List<String>> readDialect(
            String language
    ) {
        try (var reader = ResourceLoader.openReader(
                GherkinDialectProvider.class,
                "gherkin-dialect_" + language + ".json"
        )) {
            return new ObjectMapper().readValue(reader, Map.class);
        } catch (IOException e) {
            return null;
        }
    }

}
