/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


/**
 * Base type for parsed Gherkin syntax nodes in the execution model.
 * <p>
 * Each node optionally stores its source {@link Location}. Synthetic root
 * nodes may have a {@code null} location.
 * </p>
 */
public abstract class Node {

    protected final String type = getClass().getSimpleName();
    protected final es.iti.wakamiti.core.gherkin.parser.Location location;

    protected Node(
            es.iti.wakamiti.core.gherkin.parser
                    .Location location
    ) {
        this.location = location;
    }

    /**
     * Returns where this syntax node begins in the source document.
     *
     * @return the source coordinate, or {@code null} for synthetic document
     * roots
     */
    public Location getLocation() {
        return location;
    }

}
