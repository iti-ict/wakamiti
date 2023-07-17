/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.iti.wakamiti.core.gherkin.parser.GherkinDialect;
import es.iti.wakamiti.core.gherkin.parser.Location;
import es.iti.wakamiti.core.gherkin.parser.ParserException;
import es.iti.wakamiti.core.gherkin.parser.internal.ResourceLoader;

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

    public es.iti.wakamiti.core.gherkin.parser.GherkinDialect getDefaultDialect() {
        return getDialect(defaultDialect, null);
    }

    public es.iti.wakamiti.core.gherkin.parser.GherkinDialect getDialect(String language, Location location) {
        Map<String, List<String>> map = DIALECTS.computeIfAbsent(language, this::readDialect);
        if (map == null) {
            throw new ParserException.NoSuchLanguageException(language, location);
        }

        return new es.iti.wakamiti.core.gherkin.parser.GherkinDialect(language, map);
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