/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;



/**
 * Provides the Gherkin Document functionality used by Wakamiti.
 */
public class GherkinDocument extends Node {

    private final es.iti.wakamiti.core.gherkin.parser.Feature feature;

    /**
     * Creates a parsed document around its single feature declaration.
     *
     * @param feature parsed feature, or {@code null} for an empty document
     */
    public GherkinDocument(
            es.iti.wakamiti.core.gherkin.parser.Feature feature
    ) {
        super(null);
        this.feature = feature;
    }

    /**
     * Returns the feature declared by this document.
     *
     * @return the parsed feature, or {@code null} when no feature was present
     */
    public Feature getFeature() {
        return feature;
    }

}
