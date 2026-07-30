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
 * Provides the Table Row functionality used by Wakamiti.
 */
public class TableRow extends Node {

    private final List<es.iti.wakamiti.core.gherkin.parser.TableCell> cells;

    /**
     * Creates a table row at a source location.
     *
     * @param location position of the row's opening delimiter
     * @param cells    cells in left-to-right order
     */
    public TableRow(
            Location location,
            List<es.iti.wakamiti.core.gherkin.parser.TableCell> cells
    ) {
        super(location);
        this.cells = Collections.unmodifiableList(cells);
    }

    /**
     * Returns cells in their source order.
     *
     * @return an unmodifiable cell list
     */
    public List<TableCell> getCells() {
        return cells;
    }

}
