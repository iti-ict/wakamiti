/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.gherkin;

public class Comment extends Node {

    private final String text;

    public Comment(Location location, String text) {
        super(location);
        this.text = text;
    }

    public String getText() {
        return text;
    }
}