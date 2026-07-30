/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


/**
 * Defines the contract implemented by Gherkin Language Constants.
 */
public final class GherkinLanguageConstants {

    private GherkinLanguageConstants() {
    }

    /** Prefix introducing a Gherkin tag. */
    public static final String TAG_PREFIX = "@";
    /** Prefix introducing a single-line Gherkin comment. */
    public static final String COMMENT_PREFIX = "#";
    /** Separator between a localized section keyword and its title. */
    public static final String TITLE_KEYWORD_SEPARATOR = ":";
    /** Delimiter surrounding cells in a Gherkin data-table row. */
    public static final String TABLE_CELL_SEPARATOR = "|";
    /** Standard delimiter that opens and closes a Gherkin Doc String. */
    public static final String DOCSTRING_SEPARATOR = "\"\"\"";
    /** Backtick-based alternative delimiter for a Gherkin Doc String. */
    public static final String DOCSTRING_ALTERNATIVE_SEPARATOR = "```";

}
