/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


import java.util.List;


/**
 * Represents a Tagged Node node in the execution model.
 */
public interface TaggedNode {

    /**
     * Returns tags declared directly on this syntax node.
     *
     * @return tags in source order
     */
    List<Tag> getTags();

}
