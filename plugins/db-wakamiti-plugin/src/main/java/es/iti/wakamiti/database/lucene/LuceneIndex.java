/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.lucene;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.database.DatabaseHelper;
import es.iti.wakamiti.database.DatabaseStepContributor;
import es.iti.wakamiti.database.jdbc.Database;
import es.iti.wakamiti.database.jdbc.Select;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Optional;


/**
 * Minimal Lucene index wrapper used by database similar-search diagnostics.
 * <p>
 * This index is not the source of truth. The relational database is always
 * the source of truth, and in strict mode the index is rebuilt before each
 * lookup so external database changes are reflected immediately.
 */
public final class LuceneIndex implements AutoCloseable {

    private static final Logger LOGGER = WakamitiLogger.forClass(DatabaseStepContributor.class);

    private final Directory directory;
    private final Analyzer analyzer;

    /**
     * Creates a Lucene index holder.
     *
     * @param directory Lucene directory (memory or filesystem)
     * @param analyzer Lucene analyzer used to index/search text
     */
    public LuceneIndex(Directory directory, Analyzer analyzer) {
        this.directory = directory;
        this.analyzer = analyzer;
    }

    /**
     * Exposes underlying Lucene directory.
     *
     * @return directory used by this index
     */
    public Directory directory() {
        return directory;
    }

    /**
     * Exposes analyzer used by this index.
     *
     * @return analyzer used for indexing and query parsing
     */
    public Analyzer analyzer() {
        return analyzer;
    }

    /**
     * Rebuilds index contents from current table data.
     * <p>
     * Method name uses "ensure" to match previous lazy mode API, but in strict
     * mode this always performs a full rebuild.
     *
     * @param db database wrapper used to read source data
     * @param table source table name
     * @param columns source columns to index
     * @param queryTimeoutSeconds JDBC query timeout in seconds ({@code <= 0} means no timeout)
     * @throws IOException if Lucene writer cannot be created or committed
     */
    public void ensureUpToDate(Database db, String table, String[] columns, int queryTimeoutSeconds) throws IOException {
        rebuildIndex(db, table, columns, queryTimeoutSeconds);
    }

    /**
     * Internal full rebuild implementation.
     * Existing index content is replaced atomically by opening writer in
     * {@link org.apache.lucene.index.IndexWriterConfig.OpenMode#CREATE} mode.
     */
    private void rebuildIndex(Database db, String table, String[] columns, int queryTimeoutSeconds) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            String sql = db.parser().sqlSelectFrom(db.parser().format(db.table(table)), columns).toString();
            Select.Builder builder = db.select(sql);
            if (queryTimeoutSeconds > 0) {
                builder.queryTimeoutSeconds(queryTimeoutSeconds);
            }
            try (Select<String[]> select = builder.get(DatabaseHelper::format)) {
                select.forEachRow(row -> {
                    Document document = new Document();
                    for (int i = 0; i < columns.length; i++) {
                        String value = Optional.ofNullable(row[i]).orElse("");
                        document.add(new TextField(columns[i], value, Field.Store.YES));
                    }
                    try {
                        writer.addDocument(document);
                    } catch (IOException e) {
                        throw new WakamitiException("Unable to index document", e);
                    }
                });
            }
        }
    }

    /**
     * Releases Lucene resources.
     * This should be called when connection/scenario lifecycle ends.
     */
    @Override
    public void close() {
        try {
            directory.close();
        } catch (IOException e) {
            LOGGER.warn("Unable to close Lucene directory", e);
        }
        analyzer.close();
    }
}
