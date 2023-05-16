/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.commons.gherkin.internal;

public class GherkinLineSpan {
    // One-based line position
    public final int column;

    // text part of the line
    public final String text;

    public GherkinLineSpan(int column, String text) {
        this.column = column;
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
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