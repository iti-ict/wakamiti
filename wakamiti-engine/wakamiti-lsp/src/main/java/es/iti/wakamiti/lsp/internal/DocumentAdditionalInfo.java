/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


import java.util.regex.Pattern;

import com.github.curiousoddman.rgxgen.RgxGen;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.core.gherkin.parser.GherkinDocument;
import es.iti.wakamiti.core.gherkin.parser.Tag;


/**
 * Provides the Document Additional Info functionality used by Wakamiti.
 */
public class DocumentAdditionalInfo {

    /** Whether definition/implementation redefinition links are enabled. */
    public final boolean redefinitionEnabled;
    /** Feature tag that identifies a document as a reusable definition. */
    public final String redefinitionDefinitionTag;
    /** Feature tag that identifies a document as a definition implementation. */
    public final String redefinitionImplementationTag;
    /** Whether the parsed feature declares the configured definition tag. */
    public final boolean hasRedefinitionDefinitionTag;
    /** Whether the parsed feature declares the configured implementation tag. */
    public final boolean hasRedefinitionImplementationTag;
    /** Pattern used to locate and capture scenario identifiers in tag lines. */
    public final Pattern idTagPattern;
    /** Generator used by quick fixes to create IDs matching {@link #idTagPattern}. */
    public final RgxGen idTagGenerator;

    /**
     * Derives redefinition metadata from the effective configuration and parsed
     * feature.
     * <p>
     * When redefinition is disabled, definition and implementation tag names
     * are {@code null} and both classification flags are {@code false}. The ID
     * pattern and its matching value generator are initialized independently
     * because missing-ID diagnostics can still use them.
     *
     * @param effectiveConfiguration merged global, workspace and document
     *                               configuration
     * @param parsedDocument parsed Gherkin document, or {@code null} when the
     *                       source is empty or invalid
     */
    public DocumentAdditionalInfo(
            Configuration effectiveConfiguration,
            GherkinDocument parsedDocument
    ) {
        this.redefinitionEnabled = effectiveConfiguration
                .get(WakamitiConfiguration.REDEFINITION_ENABLED, Boolean.class)
                .orElse(Boolean.TRUE);

        this.idTagPattern = effectiveConfiguration
                .get(WakamitiConfiguration.ID_TAG_PATTERN, String.class)
                .map(pattern -> "@(" + pattern + ")")
                .map(Pattern::compile)
                .orElseThrow();

        this.idTagGenerator = new RgxGen(idTagPattern.pattern().replace("*", "{5}"));

        if (this.redefinitionEnabled) {
            this.redefinitionDefinitionTag = effectiveConfiguration
                    .get(WakamitiConfiguration.REDEFINITION_DEFINITION_TAG, String.class)
                    .orElse("");

            this.redefinitionImplementationTag = effectiveConfiguration
                    .get(WakamitiConfiguration.REDEFINITION_IMPLEMENTATION_TAG, String.class)
                    .orElse("");

            if (parsedDocument != null && parsedDocument.getFeature() != null) {
                this.hasRedefinitionDefinitionTag = parsedDocument
                        .getFeature()
                        .getTags()
                        .stream()
                        .map(Tag::getName)
                        .anyMatch(tag -> tag.equals("@" + redefinitionDefinitionTag));

                this.hasRedefinitionImplementationTag = parsedDocument
                        .getFeature()
                        .getTags()
                        .stream()
                        .map(Tag::getName)
                        .anyMatch(tag -> tag.equals("@" + redefinitionImplementationTag));
            } else {
                this.hasRedefinitionDefinitionTag = false;
                this.hasRedefinitionImplementationTag = false;
            }
        } else {
            this.redefinitionDefinitionTag = null;
            this.redefinitionImplementationTag = null;
            this.hasRedefinitionDefinitionTag = false;
            this.hasRedefinitionImplementationTag = false;
        }
    }

}
