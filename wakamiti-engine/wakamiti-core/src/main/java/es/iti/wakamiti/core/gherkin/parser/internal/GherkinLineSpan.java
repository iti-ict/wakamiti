/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser.internal;


/**
 * Provides the Gherkin Line Span functionality used by Wakamiti.
 */
public class GherkinLineSpan {

    /** One-based column at which this span starts in its source line. */
    public final int column;

    /** Text contained in this portion of the Gherkin source line. */
    public final String text;

    public GherkinLineSpan(
            int column,
            String text
    ) {
        this.column = column;
        this.text = text;
    }

    @Override
    public boolean equals(
            Object o
    ) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GherkinLineSpan that = (GherkinLineSpan) o;
        return column == that.column && text.equals(that.text);
    }

    @Override
    public int hashCode() {
        int result = column;
        result = 31 * result + text.hashCode();
        return result;
    }

}
