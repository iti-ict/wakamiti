/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser;

import es.iti.wakamiti.core.gherkin.parser.Feature;
import es.iti.wakamiti.core.gherkin.parser.Node;

public class GherkinDocument extends Node {

    private final es.iti.wakamiti.core.gherkin.parser.Feature feature;

    public GherkinDocument(
        es.iti.wakamiti.core.gherkin.parser.Feature feature
    ) {
        super(null);
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }

}