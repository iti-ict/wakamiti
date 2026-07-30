/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


import java.util.List;


/**
 * Provides the Step functionality used by Wakamiti.
 */
public class Step extends CommentedNode {

    private final String keyword;
    private final String text;
    private final es.iti.wakamiti.core.gherkin.parser.Node argument;

    /**
     * Creates a parsed scenario step.
     *
     * @param location position of the step keyword
     * @param keyword  localized Given/When/Then/And/But keyword
     * @param text     step sentence without the keyword
     * @param argument optional {@link DataTable} or {@link DocString}
     * @param comments comments associated with the step
     */
    public Step(
            Location location,
            String keyword,
            String text,
            es.iti.wakamiti.core.gherkin.parser.Node argument,
            List<Comment> comments
    ) {
        super(location, comments);
        this.keyword = keyword;
        this.text = text;
        this.argument = argument;
    }

    /**
     * Returns the step sentence without its keyword.
     *
     * @return the step text
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the localized keyword that introduced the step.
     *
     * @return the source keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Returns structured data attached beneath the step.
     *
     * @return a data table or document string, or {@code null}
     */
    public Node getArgument() {
        return argument;
    }

}
