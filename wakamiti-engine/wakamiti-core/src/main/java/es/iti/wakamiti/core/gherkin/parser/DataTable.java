/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser;

import es.iti.wakamiti.core.gherkin.parser.Node;
import es.iti.wakamiti.core.gherkin.parser.TableRow;

import java.util.Collections;
import java.util.List;

public class DataTable extends Node {

    private final List<TableRow> rows;

    public DataTable(List<TableRow> rows) {
        super(rows.get(0).getLocation());
        this.rows = Collections.unmodifiableList(rows);
    }

    public List<TableRow> getRows() {
        return rows;
    }
}