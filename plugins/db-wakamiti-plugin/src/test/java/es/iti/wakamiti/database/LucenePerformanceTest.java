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

import static org.assertj.core.api.Assertions.assertThat;


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

    @Test
    public void testPerformance() throws SQLException {
        int numRows = 10000;
        String descriptionBase = "Description for record %d with some extra text to make it longer and more " +
                "realistic for similarity comparison";
        insertRows(numRows, descriptionBase);

        Configuration baseConfig = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.similarSearch.maxRows", String.valueOf(numRows + 1)
        );

        String nameToSearch = "Name 9999";
        String descriptionToSearch = String.format(descriptionBase, 9999);

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
        String nameToSearch2 = "Name 5000";
        String descriptionToSearch2 = String.format(descriptionBase, 5000);
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

        LOGGER.debug("Improvement factor (cached): {}x", (double) standardTime / Math.max(1, luceneTimeCached));

        // Sanity check: timing was captured.
        assertThat(luceneTimeCached).isLessThanOrEqualTo(10000L);
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
                "database.similarSearch.maxRows", String.valueOf(numRows + 1),
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
}
