/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


/**
 * Provides the Location functionality used by Wakamiti.
 */
public class Location {

    private final int line;
    private final int column;

    /**
     * Creates a one-based source coordinate.
     *
     * @param line   source line number
     * @param column source column number
     */
    public Location(
            int line,
            int column
    ) {
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the one-based source line.
     *
     * @return the line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the one-based source column.
     *
     * @return the column number
     */
    public int getColumn() {
        return column;
    }

}
