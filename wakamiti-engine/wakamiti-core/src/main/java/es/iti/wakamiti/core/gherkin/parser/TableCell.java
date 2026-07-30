/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


/**
 * Provides the Table Cell functionality used by Wakamiti.
 */
public class TableCell extends Node {

    private final String value;

    /**
     * Creates a table cell.
     *
     * @param location position of the cell in the source row
     * @param value    unescaped textual cell value
     */
    public TableCell(
            Location location,
            String value
    ) {
        super(location);
        this.value = value;
    }

    /**
     * Returns the parsed cell content.
     *
     * @return the unescaped value
     */
    public String getValue() {
        return value;
    }

}
