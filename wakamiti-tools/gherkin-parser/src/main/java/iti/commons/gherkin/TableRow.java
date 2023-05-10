/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.gherkin;

import java.util.Collections;
import java.util.List;

public class TableRow extends Node {
    private final List<TableCell> cells;

    public TableRow(Location location, List<TableCell> cells) {
        super(location);
        this.cells = Collections.unmodifiableList(cells);
    }

    public List<TableCell> getCells() {
        return cells;
    }

}