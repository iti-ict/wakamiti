/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


public class Comment extends Node {

    private final String text;

    public Comment(
            Location location,
            String text
    ) {
        super(location);
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
