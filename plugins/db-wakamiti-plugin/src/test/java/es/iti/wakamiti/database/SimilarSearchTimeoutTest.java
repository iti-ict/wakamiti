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
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class SimilarSearchTimeoutTest {

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

    private void insertRows(int numRows, String descriptionBase) throws SQLException {
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

    private void insertRowsNoPk(int numRows, String descriptionBase) throws SQLException {
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
    public void testSimilarSearchTimeoutReturnsEmpty() throws SQLException {
        int numRows = 10000;
        Duration timeout = Duration.ofMillis(1);
        String descriptionBase = ("Description for record %d with payload " + "X".repeat(170));
        insertRows(numRows, descriptionBase);

        String expectedName = "Name 9999";
        String expectedDescription = String.format(descriptionBase, 9999);

        Configuration warmupConfig = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.similarSearch.timeout", "0"
        );
        configContributor.configurer().configure(contributor, warmupConfig);

        long start = System.nanoTime();
        Optional<Map<String, String>> result = contributor.similarBy(
                "perf_table",
                new String[]{"name", "description"},
                new Object[]{expectedName, expectedDescription}
        );
        assertThat(result).isPresent();
        long elapsedWarmupNanos = System.nanoTime() - start;
        LOGGER.debug("Elapsed warmup: {} ns", elapsedWarmupNanos);

        Configuration timeoutConfig = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.similarSearch.timeout", String.valueOf(timeout.toMillis())
        );
        configContributor.configurer().configure(contributor, timeoutConfig);

        start = System.nanoTime();
        result = contributor.similarBy(
                "perf_table",
                new String[]{"name", "description"},
                new Object[]{expectedName, expectedDescription}
        );
        long elapsedNanos = System.nanoTime() - start;
        LOGGER.debug("Elapsed timeout: {} ns", elapsedNanos);

        assertThat(result).isEmpty();
        assertThat(elapsedWarmupNanos).isGreaterThan(timeout.toNanos());
        assertThat(elapsedNanos).isGreaterThan(timeout.toNanos());
    }

    @Test
    public void testSimilarSearchTimeoutRespectedWithoutPrimaryKey() throws SQLException {
        int numRows = 10000;
        String descriptionBase = ("Description for record %d with payload " + "X".repeat(170));
        insertRowsNoPk(numRows, descriptionBase);

        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.similarSearch.timeout", "1"
        );
        configContributor.configurer().configure(contributor, config);

        long start = System.currentTimeMillis();
        Optional<Map<String, String>> result = contributor.similarBy(
                "no_pk_table",
                new String[]{"name", "description"},
                new Object[]{"Name not found", "Description not found " + "Y".repeat(170)}
        );
        long elapsed = System.currentTimeMillis() - start;

        assertThat(result).isEmpty();
        assertThat(elapsed).isLessThanOrEqualTo(2000L);
    }

    @Test
    public void testSimilarSearchReflectsExternalChangesWithAndWithoutPk() throws SQLException {
        insertRows(3000, "Description for record %d");
        insertRowsNoPk(3000, "No PK description %d");

        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS
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
    }
}
