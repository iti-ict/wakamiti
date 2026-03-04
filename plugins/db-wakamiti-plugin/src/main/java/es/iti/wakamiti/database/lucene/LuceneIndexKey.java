/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.lucene;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * Immutable cache key used to identify Lucene indexes in memory.
 * <p>
 * Why these fields:
 * <ul>
 *   <li>{@code alias}: different connections may point to different databases.</li>
 *   <li>{@code table}: each table has independent data and vocabulary.</li>
 *   <li>{@code columns}: candidate retrieval depends on selected columns.</li>
 * </ul>
 */
public final class LuceneIndexKey {

    private final String alias;
    private final String table;
    private final List<String> columns;

    /**
     * Builds immutable key.
     * Incoming column array is defensively copied to prevent external mutation.
     *
     * @param alias connection alias
     * @param table normalized table name
     * @param columns ordered column list used in similar search
     */
    public LuceneIndexKey(String alias, String table, String[] columns) {
        this.alias = alias;
        this.table = table;
        this.columns = Arrays.asList(columns.clone());
    }

    /**
     * @return connection alias used by this key
     */
    public String alias() {
        return alias;
    }

    /**
     * @return normalized table name used by this key
     */
    public String table() {
        return table;
    }

    /**
     * @return immutable ordered column list used by this key
     */
    public List<String> columns() {
        return columns;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LuceneIndexKey that = (LuceneIndexKey) obj;
        return Objects.equals(alias, that.alias)
                && Objects.equals(table, that.table)
                && Objects.equals(columns, that.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, table, columns);
    }
}
