/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.gherkin;

public abstract class Node {

    protected final String type = getClass().getSimpleName();
    protected final Location location;

    protected Node(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }


}