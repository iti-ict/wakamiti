/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.lucene;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.database.DatabaseHelper;
import es.iti.wakamiti.database.jdbc.Database;
import es.iti.wakamiti.database.jdbc.Select;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;


/**
 * Minimal Lucene index wrapper used by database similar-search diagnostics.
 * <p>
 * This index is not the source of truth. The relational database is always
 * the source of truth, and index contents are refreshed from DB data before
 * each lookup so external changes are reflected immediately.
 */
public final class LuceneIndex implements AutoCloseable {

    private static final Logger LOGGER = WakamitiLogger.forClass(LuceneIndex.class);
    private static final String DOC_ID_FIELD = "__wakamiti_doc_id";
    private static final byte[] HASH_SEPARATOR = new byte[]{0x1F};

    private final Directory directory;
    private final Analyzer analyzer;
    private final Map<String, String> rowHashesByDocId = new HashMap<>();
    private boolean incrementalStateInitialized;

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
     * Refreshes index contents from current table data and executes timeout
     * checks during row iteration.
     *
     * @param db database wrapper used to read source data
     * @param table source table name
     * @param columns source columns to index
     * @param queryTimeoutSeconds JDBC query timeout in seconds ({@code <= 0} means no timeout)
     * @param timeoutCheck cooperative timeout guard executed while indexing rows
     * @throws IOException if Lucene writer cannot be created or committed
     */
    public void ensureUpToDate(
            Database db, String table, String[] columns, int queryTimeoutSeconds, Runnable timeoutCheck) throws IOException {
        String normalizedTable = db.table(table);
        String[] pkColumns = db.primaryKey(normalizedTable)
                .map(db.parser()::format)
                .toArray(String[]::new);

        if (pkColumns.length == 0) {
            rebuildIndex(db, normalizedTable, columns, queryTimeoutSeconds, timeoutCheck);
            return;
        }
        if (!incrementalStateInitialized) {
            rebuildIndexWithPrimaryKey(db, normalizedTable, columns, pkColumns, queryTimeoutSeconds, timeoutCheck);
            incrementalStateInitialized = true;
            return;
        }
        refreshIndexWithPrimaryKey(db, normalizedTable, columns, pkColumns, queryTimeoutSeconds, timeoutCheck);
    }

    /**
     * Internal full rebuild implementation.
     * Existing index content is replaced atomically by opening writer in
     * {@link org.apache.lucene.index.IndexWriterConfig.OpenMode#CREATE} mode.
     */
    private void rebuildIndex(
            Database db, String table, String[] columns, int queryTimeoutSeconds, Runnable timeoutCheck) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            String sql = db.parser().sqlSelectFrom(db.parser().format(db.table(table)), columns).toString();
            Select.Builder builder = selectWithTimeout(db, sql, queryTimeoutSeconds);
            try (Select<String[]> select = builder.get(DatabaseHelper::format)) {
                select.forEachRow(row -> {
                    timeoutCheck.run();
                    Document document = toSearchDocument(columns, row, null);
                    try {
                        writer.addDocument(document);
                    } catch (IOException e) {
                        throw new WakamitiException("Unable to index document", e);
                    }
                });
            }
        }
        rowHashesByDocId.clear();
        incrementalStateInitialized = false;
    }

    /**
     * Builds baseline index when table has a primary key.
     * This resets index and stores PK+hash state for subsequent incremental refreshes.
     */
    private void rebuildIndexWithPrimaryKey(
            Database db, String table, String[] columns, String[] pkColumns, int queryTimeoutSeconds, Runnable timeoutCheck)
            throws IOException {
        Projection projection = Projection.from(pkColumns, columns);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            String sql = db.parser().sqlSelectFrom(db.parser().format(table), projection.selectColumns()).toString();
            Select.Builder builder = selectWithTimeout(db, sql, queryTimeoutSeconds);
            rowHashesByDocId.clear();
            try (Select<String[]> select = builder.get(DatabaseHelper::format)) {
                select.forEachRow(row -> {
                    timeoutCheck.run();
                    String[] pkValues = projection.pickPrimaryKeyValues(row);
                    String[] dataValues = projection.pickDataValues(row);
                    String docId = docIdFromPrimaryKey(pkValues);
                    String hash = contentHash(dataValues);
                    Document document = toSearchDocument(columns, dataValues, docId);
                    try {
                        writer.addDocument(document);
                    } catch (IOException e) {
                        throw new WakamitiException("Unable to index document", e);
                    }
                    rowHashesByDocId.put(docId, hash);
                });
            }
        }
    }

    /**
     * Applies PK+hash incremental refresh strategy.
     * Only changed/new/deleted rows produce Lucene writes.
     */
    private void refreshIndexWithPrimaryKey(
            Database db, String table, String[] columns, String[] pkColumns, int queryTimeoutSeconds, Runnable timeoutCheck)
            throws IOException {
        Projection projection = Projection.from(pkColumns, columns);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            String sql = db.parser().sqlSelectFrom(db.parser().format(table), projection.selectColumns()).toString();
            Select.Builder builder = selectWithTimeout(db, sql, queryTimeoutSeconds);
            Set<String> seenDocIds = new HashSet<>();
            try (Select<String[]> select = builder.get(DatabaseHelper::format)) {
                select.forEachRow(row -> {
                    timeoutCheck.run();
                    String[] pkValues = projection.pickPrimaryKeyValues(row);
                    String[] dataValues = projection.pickDataValues(row);
                    String docId = docIdFromPrimaryKey(pkValues);
                    String hash = contentHash(dataValues);
                    seenDocIds.add(docId);
                    if (!Objects.equals(rowHashesByDocId.get(docId), hash)) {
                        Document document = toSearchDocument(columns, dataValues, docId);
                        try {
                            writer.updateDocument(new Term(DOC_ID_FIELD, docId), document);
                        } catch (IOException e) {
                            throw new WakamitiException("Unable to update index document", e);
                        }
                        rowHashesByDocId.put(docId, hash);
                    }
                });
            }
            Iterator<Map.Entry<String, String>> iterator = rowHashesByDocId.entrySet().iterator();
            while (iterator.hasNext()) {
                String staleDocId = iterator.next().getKey();
                if (!seenDocIds.contains(staleDocId)) {
                    timeoutCheck.run();
                    writer.deleteDocuments(new Term(DOC_ID_FIELD, staleDocId));
                    iterator.remove();
                }
            }
        }
    }

    private Select.Builder selectWithTimeout(Database db, String sql, int queryTimeoutSeconds) {
        Select.Builder builder = db.select(sql);
        if (queryTimeoutSeconds > 0) {
            builder.queryTimeoutSeconds(queryTimeoutSeconds);
        }
        return builder;
    }

    private Document toSearchDocument(String[] columns, String[] rowValues, String docId) {
        Document document = new Document();
        if (docId != null) {
            document.add(new StringField(DOC_ID_FIELD, docId, Field.Store.NO));
        }
        for (int i = 0; i < columns.length; i++) {
            String value = Optional.ofNullable(rowValues[i]).orElse("");
            document.add(new TextField(columns[i], value, Field.Store.YES));
        }
        return document;
    }

    private String docIdFromPrimaryKey(String[] pkValues) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < pkValues.length; i++) {
            if (i > 0) {
                builder.append('\u001F');
            }
            builder.append(Optional.ofNullable(pkValues[i]).orElse("<null>"));
        }
        return builder.toString();
    }

    private String contentHash(String[] dataValues) {
        MessageDigest digest = newSha256();
        for (String value : dataValues) {
            byte[] bytes = Optional.ofNullable(value).orElse("").getBytes(StandardCharsets.UTF_8);
            digest.update(bytes);
            digest.update(HASH_SEPARATOR);
        }
        byte[] hash = digest.digest();
        StringBuilder builder = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private MessageDigest newSha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static final class Projection {

        private final String[] selectColumns;
        private final int[] pkIndexes;
        private final int[] dataIndexes;

        private Projection(String[] selectColumns, int[] pkIndexes, int[] dataIndexes) {
            this.selectColumns = selectColumns;
            this.pkIndexes = pkIndexes;
            this.dataIndexes = dataIndexes;
        }

        static Projection from(String[] pkColumns, String[] dataColumns) {
            LinkedHashSet<String> merged = new LinkedHashSet<>();
            Collections.addAll(merged, pkColumns);
            Collections.addAll(merged, dataColumns);
            String[] selected = merged.toArray(String[]::new);
            Map<String, Integer> indexes = new HashMap<>();
            for (int i = 0; i < selected.length; i++) {
                indexes.put(selected[i], i);
            }
            int[] pkIndexes = Stream.of(pkColumns).mapToInt(indexes::get).toArray();
            int[] dataIndexes = Stream.of(dataColumns).mapToInt(indexes::get).toArray();
            return new Projection(selected, pkIndexes, dataIndexes);
        }

        String[] selectColumns() {
            return selectColumns;
        }

        String[] pickPrimaryKeyValues(String[] row) {
            String[] values = new String[pkIndexes.length];
            for (int i = 0; i < pkIndexes.length; i++) {
                values[i] = row[pkIndexes[i]];
            }
            return values;
        }

        String[] pickDataValues(String[] row) {
            String[] values = new String[dataIndexes.length];
            for (int i = 0; i < dataIndexes.length; i++) {
                values[i] = row[dataIndexes[i]];
            }
            return values;
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
