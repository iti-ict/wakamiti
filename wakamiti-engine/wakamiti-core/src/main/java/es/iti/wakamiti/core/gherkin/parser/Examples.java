/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser;

import es.iti.wakamiti.core.gherkin.parser.Location;
import es.iti.wakamiti.core.gherkin.parser.Node;
import es.iti.wakamiti.core.gherkin.parser.TableRow;
import es.iti.wakamiti.core.gherkin.parser.Tag;

import java.util.Collections;
import java.util.List;

public class Examples extends Node {

    private final List<Tag> tags;
    private final String keyword;
    private final String name;
    private final String description;
    private final TableRow tableHeader;
    private final List<TableRow> tableBody;

    public Examples(Location location, List<Tag> tags, String keyword, String name, String description, TableRow tableHeader, List<TableRow> tableBody) {
        super(location);
        this.tags = Collections.unmodifiableList(tags);
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.tableHeader = tableHeader;
        this.tableBody = tableBody != null ? Collections.unmodifiableList(tableBody) : null;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<TableRow> getTableBody() {
        return tableBody;
    }

    public TableRow getTableHeader() {
        return tableHeader;
    }

    public List<Tag> getTags() {
        return tags;
    }
}