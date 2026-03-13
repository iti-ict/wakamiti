/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import es.iti.wakamiti.database.jdbc.Database;
import es.iti.wakamiti.database.lucene.LuceneIndex;
import es.iti.wakamiti.database.lucene.LuceneIndexFactory;
import es.iti.wakamiti.database.lucene.LuceneIndexKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class LucenePerformanceTest {

    private static final Logger LOGGER = WakamitiLogger.forName("es.iti.wakamiti.database");

    private static final String URL = "jdbc:h2:mem:perf_test;MODE=MySQL;";
    private static final String USER = "sa";
    private static final String PASS = "";
    private static Connection h2;

    private DatabaseStepContributor contributor;
    private DatabaseConfigContributor configContributor;

    @BeforeClass
    public static void setup() throws SQLException {
        h2 = DriverManager.getConnection(URL, USER, PASS);
        h2.createStatement().execute(
                "CREATE TABLE perf_table (id INT PRIMARY KEY, name VARCHAR(255), description VARCHAR(255))");
        h2.createStatement().execute(
                "CREATE TABLE no_pk_table (name VARCHAR(255), description VARCHAR(255))");
    }

    @AfterClass
    public static void shutdown() throws SQLException {
        h2.close();
    }

    @Before
    public void init() throws SQLException {
        contributor = new DatabaseStepContributor();
        configContributor = new DatabaseConfigContributor();
        h2.createStatement().execute("TRUNCATE TABLE perf_table");
        h2.createStatement().execute("TRUNCATE TABLE no_pk_table");
    }

    private void insertRows(
            int numRows,
            String descriptionBase
    ) throws SQLException {
        LOGGER.debug("Inserting {} rows...", numRows);
        h2.setAutoCommit(false);
        try (PreparedStatement ps = h2.prepareStatement(
                "INSERT INTO perf_table (id, name, description) VALUES (?, ?, ?)")
        ) {
            for (int i = 0; i < numRows; i++) {
                ps.setInt(1, i);
                ps.setString(2, "Name " + i);
                ps.setString(3, String.format(descriptionBase, i));
                ps.addBatch();
                if (i % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
        }
        h2.commit();
        h2.setAutoCommit(true);
    }

    private void insertRowsNoPk(
            int numRows,
            String descriptionBase
    ) throws SQLException {
        LOGGER.debug("Inserting {} rows in no_pk_table...", numRows);
        h2.setAutoCommit(false);
        try (PreparedStatement ps = h2.prepareStatement(
                "INSERT INTO no_pk_table (name, description) VALUES (?, ?)")
        ) {
            for (int i = 0; i < numRows; i++) {
                ps.setString(1, "NoPk Name " + i);
                ps.setString(2, String.format(descriptionBase, i));
                ps.addBatch();
                if (i % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
        }
        h2.commit();
        h2.setAutoCommit(true);
    }

    @Test
    public void testPerformance() throws SQLException {
        int numRows = 10000;
        String descriptionBase = "Description for record %d with some extra text to make it longer and more " +
                "realistic for similarity comparison";
        insertRows(numRows, descriptionBase);
        h2.createStatement().execute(
                "UPDATE perf_table SET name = 'TARGET_UNIQUE_9999', description = 'TARGET_UNIQUE_DESC_9999' WHERE id = 9999");
        h2.createStatement().execute(
                "UPDATE perf_table SET name = 'TARGET_UNIQUE_5000', description = 'TARGET_UNIQUE_DESC_5000' WHERE id = 5000");

        Configuration baseConfig = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS
        );

        String nameToSearch = "TARGET_UNIQUE_9999";
        String descriptionToSearch = "TARGET_UNIQUE_DESC_9999";

        // Standard Search
        configContributor.configurer().configure(
                contributor, baseConfig.appendFromPairs("database.similarSearch.lucene.enabled", "false"));

        long start = System.currentTimeMillis();
        Optional<Map<String, String>> result1 = contributor.similarBy(
                "perf_table",
                new String[]{"name", "description"},
                new Object[]{nameToSearch, descriptionToSearch}
        );
        long end = System.currentTimeMillis();
        long standardTime = end - start;
        LOGGER.debug("Standard search time: {} ms", standardTime);
        assertThat(result1).isPresent();

        // Lucene Search
        configContributor.configurer().configure(
                contributor, baseConfig.appendFromPairs(
                        "database.similarSearch.lucene.enabled", "true",
                        "database.similarSearch.lucene.topK", "10"
                )
        );

        // First search includes indexing time
        start = System.currentTimeMillis();
        Optional<Map<String, String>> result2 = contributor.similarBy(
                "perf_table",
                new String[]{"name", "description"},
                new Object[]{nameToSearch, descriptionToSearch}
        );
        end = System.currentTimeMillis();
        long luceneTimeWithIndexing = end - start;
        LOGGER.debug("Lucene search time (with indexing): {} ms", luceneTimeWithIndexing);
        assertThat(result2).isPresent();

        // Second search (index already built)
        String nameToSearch2 = "TARGET_UNIQUE_5000";
        String descriptionToSearch2 = "TARGET_UNIQUE_DESC_5000";
        start = System.currentTimeMillis();
        Optional<Map<String, String>> result3 = contributor.similarBy(
                "perf_table",
                new String[]{"name", "description"},
                new Object[]{nameToSearch2, descriptionToSearch2}
        );
        end = System.currentTimeMillis();
        long luceneTimeCached = end - start;
        LOGGER.debug("Lucene search time (cached): {} ms",  luceneTimeCached);
        assertThat(result3).isPresent();
    }

    @Test
    public void testSimilarSearchTimeoutReturnsEmpty() throws SQLException {
        int numRows = 10000;
        String descriptionBase = "Description for record %d with some extra text to make it longer and more " +
                "realistic for similarity comparison";
        insertRows(numRows, descriptionBase);

        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.similarSearch.timeout", "1",
                "database.similarSearch.lucene.enabled", "true",
                "database.similarSearch.lucene.topK", "10"
        );
        configContributor.configurer().configure(contributor, config);

        Optional<Map<String, String>> result = contributor.similarBy(
                "perf_table",
                new String[]{"name", "description"},
                new Object[]{"Name 9999", String.format(descriptionBase, 9999)}
        );

        assertThat(result).isEmpty();
    }

    @Test
    public void testSimilarSearchTimeoutRespectedInSqlFallback() throws SQLException {
        int numRows = 10000;
        String descriptionBase = ("Description for record %d with payload " + "X".repeat(170));
        insertRows(numRows, descriptionBase);

        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.similarSearch.timeout", "1",
                "database.similarSearch.lucene.enabled", "false"
        );
        configContributor.configurer().configure(contributor, config);

        long start = System.currentTimeMillis();
        Optional<Map<String, String>> result = contributor.similarBy(
                "perf_table",
                new String[]{"name", "description"},
                new Object[]{"Name not found", "Description not found " + "Y".repeat(170)}
        );
        long elapsed = System.currentTimeMillis() - start;

        LOGGER.debug("Time elapsed: {} ms", elapsed);
        assertThat(result).isEmpty();
        assertThat(elapsed).isLessThanOrEqualTo(2000L);
    }

    @Test
    public void testLuceneReindexExecutesTimeoutGuardDuringRowIteration() throws Exception {
        int numRows = 200;
        String descriptionBase = "Description for record %d";
        insertRows(numRows, descriptionBase);

        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS
        );
        configContributor.configurer().configure(contributor, config);

        Database db = Database.from(contributor.connection());
        String[] columns = new String[]{"name", "description"};
        LuceneIndexKey key = new LuceneIndexKey("default", db.table("perf_table"), columns);
        AtomicInteger checks = new AtomicInteger();

        try (LuceneIndex index = LuceneIndexFactory.createIndex(key, null)) {
            assertThatThrownBy(() -> index.ensureUpToDate(
                    db, "perf_table", columns, 0, () -> {
                        if (checks.incrementAndGet() >= 3) {
                            throw new RuntimeException("timeout");
                        }
                    }))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("timeout");
        }

        assertThat(checks.get()).isGreaterThanOrEqualTo(3);
    }

    @Test
    public void testLuceneFullRefreshWithoutPkExecutesTimeoutGuardDuringRowIteration() throws Exception {
        int numRows = 200;
        String descriptionBase = "No PK description %d";
        insertRowsNoPk(numRows, descriptionBase);

        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS
        );
        configContributor.configurer().configure(contributor, config);

        Database db = Database.from(contributor.connection());
        String[] columns = new String[]{"name", "description"};
        LuceneIndexKey key = new LuceneIndexKey("default", db.table("no_pk_table"), columns);
        AtomicInteger checks = new AtomicInteger();

        try (LuceneIndex index = LuceneIndexFactory.createIndex(key, null)) {
            assertThatThrownBy(() -> index.ensureUpToDate(
                    db, "no_pk_table", columns, 0, () -> {
                        if (checks.incrementAndGet() >= 3) {
                            throw new RuntimeException("timeout");
                        }
                    }))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("timeout");
        }

        assertThat(checks.get()).isGreaterThanOrEqualTo(3);
    }

    @Test
    public void testDynamicRefreshStrategyReflectsExternalChangesWithAndWithoutPk() throws Exception {
        insertRows(3000, "Description for record %d");
        insertRowsNoPk(3000, "No PK description %d");

        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.similarSearch.lucene.enabled", "true",
                "database.similarSearch.lucene.topK", "10"
        );
        configContributor.configurer().configure(contributor, config);

        String pkName = "UNIQUE_BEFORE_TOKEN_QQ1";
        String pkDesc = "UNIQUE_BEFORE_LONG_PAYLOAD_QQ1";
        String noPkName = "UNIQUE_BEFORE_TOKEN_QQ2";
        String noPkDesc = "UNIQUE_BEFORE_LONG_PAYLOAD_QQ2";

        h2.createStatement().execute(
                "UPDATE perf_table SET name = 'UNIQUE_BEFORE_TOKEN_QQ1', description = 'UNIQUE_BEFORE_LONG_PAYLOAD_QQ1' WHERE id = 2999");
        h2.createStatement().execute(
                "UPDATE no_pk_table SET name = 'UNIQUE_BEFORE_TOKEN_QQ2', description = 'UNIQUE_BEFORE_LONG_PAYLOAD_QQ2' WHERE name = 'NoPk Name 2999'");

        assertThat(contributor.similarBy(
                "perf_table",
                new String[]{"name", "description"},
                new Object[]{pkName, pkDesc}
        )).isPresent();
        assertThat(contributor.similarBy(
                "no_pk_table",
                new String[]{"name", "description"},
                new Object[]{noPkName, noPkDesc}
        )).isPresent();

        h2.createStatement().execute(
                "UPDATE perf_table SET name = 'ZZZ_AFTER_TOKEN_7721', description = 'COMPLETELY_DIFFERENT_PAYLOAD_PK_7721' WHERE id = 2999");
        h2.createStatement().execute(
                "UPDATE no_pk_table SET name = 'ZZZ_AFTER_TOKEN_7722', description = 'COMPLETELY_DIFFERENT_PAYLOAD_NOPK_7722' WHERE name = 'UNIQUE_BEFORE_TOKEN_QQ2'");

        assertThat(contributor.similarBy(
                "perf_table",
                new String[]{"name", "description"},
                new Object[]{pkName, pkDesc}
        )).isEmpty();
        assertThat(contributor.similarBy(
                "no_pk_table",
                new String[]{"name", "description"},
                new Object[]{noPkName, noPkDesc}
        )).isEmpty();

        assertThat(contributor.similarBy(
                "perf_table",
                new String[]{"name", "description"},
                new Object[]{"ZZZ_AFTER_TOKEN_7721", "COMPLETELY_DIFFERENT_PAYLOAD_PK_7721"}
        )).isPresent();
        assertThat(contributor.similarBy(
                "no_pk_table",
                new String[]{"name", "description"},
                new Object[]{"ZZZ_AFTER_TOKEN_7722", "COMPLETELY_DIFFERENT_PAYLOAD_NOPK_7722"}
        )).isPresent();
    }
}
