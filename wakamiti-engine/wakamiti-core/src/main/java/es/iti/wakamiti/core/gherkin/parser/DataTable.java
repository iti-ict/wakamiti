/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


import java.util.Collections;
import java.util.List;


/**
 * Provides the Data Table functionality used by Wakamiti.
 */
public class DataTable extends Node {

    private final List<TableRow> rows;

    /**
     * Creates a table argument and derives its location from the first row.
     *
     * @param rows non-empty rows in source order
     * @throws IndexOutOfBoundsException if {@code rows} is empty
     */
    public DataTable(
            List<TableRow> rows
    ) {
        super(rows.get(0).getLocation());
        this.rows = Collections.unmodifiableList(rows);
    }

    /**
     * Returns the table rows, including any header row.
     *
     * @return an unmodifiable list in source order
     */
    public List<TableRow> getRows() {
        return rows;
    }

}
