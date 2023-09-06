/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser;

import es.iti.wakamiti.core.gherkin.parser.Location;
import es.iti.wakamiti.core.gherkin.parser.Node;

public class TableCell extends Node {
    private final String value;

    public TableCell(Location location, String value) {
        super(location);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}