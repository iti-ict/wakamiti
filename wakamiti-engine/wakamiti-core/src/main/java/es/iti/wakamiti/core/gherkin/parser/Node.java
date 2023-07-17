/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser;

import es.iti.wakamiti.core.gherkin.parser.Location;

public abstract class Node {

    protected final String type = getClass().getSimpleName();
    protected final es.iti.wakamiti.core.gherkin.parser.Location location;

    protected Node(es.iti.wakamiti.core.gherkin.parser.Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }


}