/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


import java.util.Collections;
import java.util.List;


/**
 * Provides the Examples functionality used by Wakamiti.
 */
public class Examples extends Node {

    private final List<Tag> tags;
    private final String keyword;
    private final String name;
    private final String description;
    private final TableRow tableHeader;
    private final List<TableRow> tableBody;

    /**
     * Creates an examples block for parameterizing a scenario outline.
     *
     * @param location    position of the examples keyword
     * @param tags        tags applied to all rows in this block
     * @param keyword     localized examples keyword
     * @param name        optional block name
     * @param description optional descriptive text
     * @param tableHeader row defining placeholder names
     * @param tableBody   substitution rows, or {@code null} when absent
     */
    public Examples(
            Location location,
            List<Tag> tags,
            String keyword,
            String name,
            String description,
            TableRow tableHeader,
            List<TableRow> tableBody
    ) {
        super(location);
        this.tags = Collections.unmodifiableList(tags);
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.tableHeader = tableHeader;
        this.tableBody = tableBody != null ? Collections.unmodifiableList(tableBody) : null;
    }

    /**
     * Returns the localized keyword that introduced this block.
     *
     * @return the examples keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Returns the examples block's optional title.
     *
     * @return the block name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns descriptive text between the title and table.
     *
     * @return the block description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns rows that provide concrete placeholder values.
     *
     * @return an unmodifiable list, or {@code null} when the block has no body
     */
    public List<TableRow> getTableBody() {
        return tableBody;
    }

    /**
     * Returns the row that names scenario-outline placeholders.
     *
     * @return the examples table header
     */
    public TableRow getTableHeader() {
        return tableHeader;
    }

    /**
     * Returns tags scoped to this examples block.
     *
     * @return an unmodifiable list in source order
     */
    public List<Tag> getTags() {
        return tags;
    }

}
