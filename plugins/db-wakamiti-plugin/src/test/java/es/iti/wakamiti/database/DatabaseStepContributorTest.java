/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import es.iti.wakamiti.api.util.MatcherAssertion;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.database.exception.SQLRuntimeException;
import es.iti.wakamiti.database.jdbc.Database;
import es.iti.wakamiti.database.jdbc.Select;
import org.h2.tools.RunScript;
import org.junit.*;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.hamcrest.Matchers.comparesEqualTo;


public class DatabaseStepContributorTest {

    private static final Logger LOGGER = WakamitiLogger.forName("es.iti.wakamiti.database");
    private static final String URL = "jdbc:h2:mem:test;MODE=MySQL;";
    private static final String USER = "sa";
    private static final String PASS = "";

    private static Connection h2;

    private DatabaseConfigContributor configContributor;
    private DatabaseStepContributor contributor;

    @BeforeClass
    public static void setup() throws SQLException, FileNotFoundException {
        h2 = DriverManager.getConnection(URL, USER, PASS);
        RunScript.execute(h2, new FileReader("src/test/resources/db/create-schema.sql"));
    }

    @AfterClass
    public static void shutdown() throws SQLException {
        h2.close();
    }

    @Before
    public void init() throws SQLException, FileNotFoundException {
        contributor = new DatabaseStepContributor();
        configContributor = new DatabaseConfigContributor();

        RunScript.execute(h2, new FileReader("src/test/resources/db/dml.sql"));
    }

    @After
    public void finish() throws SQLException, FileNotFoundException {
        RunScript.execute(h2, new FileReader("src/test/resources/db/clean.sql"));
        contributor.releaseConnection();
    }

    @Test
    public void testDefaultConnection() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "true"
        );

        // Act
        configContributor.configurer().configure(contributor, config);

        // Check
        assertThatNoException();
    }

    @Test
    public void testConnectionWhenMultipleDataSources() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.datasource.db1.connection.url", URL,
                "database.datasource.db1.connection.username", USER,
                "database.datasource.db1.connection.password", PASS,
                "database.datasource.db2.connection.url", "jdbc:h2:mem:test2",
                "database.datasource.db2.connection.username", USER,
                "database.datasource.db2.connection.password", PASS,
                "database.datasource.db2.metadata.caseSensitivity", "lower_cased"
        );

        // Act
        configContributor.configurer().configure(contributor, config);

        // Check
        assertThat(contributor.connection().parameters().url()).isEqualTo(URL);
    }

    @Test(expected = WakamitiException.class)
    public void testConnectionWhenNoDatabasesFound() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration();
        configContributor.configurer().configure(contributor, config);

        // Act
        try {
            contributor.connection();

            // Check
        } catch (WakamitiException e) {
            assertThat(e).hasMessage("Bad jdbc url");
            throw e;
        }
    }

    @Test(expected = WakamitiException.class)
    public void testConnectionWhenNoHealthcheckAndNoDatabasesFound() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration();
        configContributor.configurer().configure(contributor, config);

        // Act
        try {
            contributor.setHealthcheck(false);
            contributor.connection();

            // Check
        } catch (WakamitiException e) {
            assertThat(e.getMessage()).isEqualTo("There is no default connection");
            throw e;
        }

    }

    @Test
    public void testAddDefaultConnectionWhenExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.setConnectionParameters("jdbc:h2:mem:test2", "sa", "");

        // Check
        assertThat(contributor.connection().parameters().url()).isEqualTo("jdbc:h2:mem:test2");
    }

    @Test
    public void testAddConnectionWhenExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.datasource.db1.connection.url", URL,
                "database.datasource.db1.connection.username", USER,
                "database.datasource.db1.connection.password", PASS,
                "database.datasource.db2.connection.url", "jdbc:h2:mem:test2",
                "database.datasource.db2.connection.username", USER,
                "database.datasource.db2.connection.password", PASS,
                "database.datasource.db2.metadata.caseSensitivity", "lower_cased"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.setConnectionParameters("jdbc:h2:mem:test2", "sa", "", "db1");

        // Check
        assertThat(contributor.connection().parameters().url()).isEqualTo("jdbc:h2:mem:test2");
    }

    @Test
    public void testSelectData() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        String sql = "SELECT * FROM client WHERE id = 1";

        // Act
        Object result = contributor.selectData(new Document(sql));
        LOGGER.debug("Result: {}", result);

        // Check
        Database db = Database.from(contributor.connection());
        String table = db.table("client");
        assertThat(result).isInstanceOf(ArrayNode.class);
        assertThat((ArrayNode) result).isNotEmpty().hasSize(1);
        assertThat(((ArrayNode) result).get(0).get(db.column(table, "first_name")).asText())
                .isEqualTo("Rosa");
        assertThat(((ArrayNode) result).get(0).get(db.column(table, "second_name")).asText())
                .isEqualTo("Melano");
        assertThat(((ArrayNode) result).get(0).get(db.column(table, "active")).asText())
                .isEqualTo("TRUE");
        assertThat(((ArrayNode) result).get(0).get(db.column(table, "birth_date")).asText())
                .isEqualTo("1980-12-25");
    }

    @Test
    public void testSelectDataWhenNull() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        contributor.executeSQLScript(new Document("UPDATE client SET second_name = null WHERE id = 1"));

        String sql = "SELECT * FROM client WHERE id = 1";

        // Act
        Object result = contributor.selectData(new Document(sql));
        LOGGER.debug("Result: {}", result);

        // Check
        Database db = Database.from(contributor.connection());
        String table = db.table("client");
        assertThat(result).isInstanceOf(ArrayNode.class);
        assertThat((ArrayNode) result).isNotEmpty().hasSize(1);
        assertThat(((ArrayNode) result).get(0).get(db.column(table, "first_name")).asText())
                .isEqualTo("Rosa");
        assertThat(((ArrayNode) result).get(0).get(db.column(table, "second_name")).asText())
                .isEqualTo("null");
        assertThat(((ArrayNode) result).get(0).get(db.column(table, "active")).asText())
                .isEqualTo("TRUE");
        assertThat(((ArrayNode) result).get(0).get(db.column(table, "birth_date")).asText())
                .isEqualTo("1980-12-25");
    }

    @Test
    public void testSelectDataWhenNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        String sql = "SELECT * FROM client WHERE id = 2";

        // Act
        Object result = contributor.selectData(new Document(sql));
        LOGGER.debug("Result: {}", result);

        // Check
        assertThat(result).isInstanceOf(ArrayNode.class);
        assertThat((ArrayNode) result).isEmpty();
    }

    @Test(expected = SQLRuntimeException.class)
    public void testSelectDataWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        String sql = "SELECT * FROM xxxxx WHERE id = 2";

        // Act
        contributor.selectData(new Document(sql));

        // Check
        // Exception is thrown
    }

    @Test(expected = SQLRuntimeException.class)
    public void testSelectDataWhenColumnNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        String sql = "SELECT * FROM client WHERE xxx = 2";

        // Act
        contributor.selectData(new Document(sql));


        // Check
        // Exception is thrown
    }

    @Test
    public void testSwitchConnection() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.datasource.db1.connection.url", URL,
                "database.datasource.db1.connection.username", USER,
                "database.datasource.db1.connection.password", PASS,
                "database.datasource.db2.connection.url", "jdbc:h2:mem:test2",
                "database.datasource.db2.connection.username", USER,
                "database.datasource.db2.connection.password", PASS,
                "database.datasource.db2.metadata.caseSensitivity", "lower_cased"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.switchConnection("db2");

        // Check
        assertThat(contributor.connection().parameters().url()).isEqualTo("jdbc:h2:mem:test2");
    }


    @Test
    public void testInsertFromDataTableWhenSingleRow() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        contributor.insertFromDataTable(client.table(), new DataTable(new String[][]{
                client.columns(),
                new String[]{"2", "Ester", "Colero", "1", "2000-02-01", "<null>"}
        }));

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).contains(
                    new String[]{"2", "Ester", "Colero", "TRUE", "2000-02-01", null}
            );
        }
    }

    @Test
    public void testInsertFromDataTableWhenSingleRowAndNull() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        contributor.insertFromDataTable(client.table(), new DataTable(new String[][]{
                client.columns(),
                new String[]{"2", "Ester", "Colero", "<null>", "2000-02-01", "2024-07-22 13:00:00"}
        }));

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).contains(
                    new String[]{"2", "Ester", "Colero", null, "2000-02-01", "2024-07-22 13:00:00.000"}
            );
        }
    }

    @Test
    public void testInsertFromDataTableWhenMultipleRow() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        contributor.insertFromDataTable(client.table(), new DataTable(new String[][]{
                client.columns(),
                new String[]{"2", "Ester", "Colero", "1", "2000-02-01", "<null>"},
                new String[]{"3", "Elca", "Puio", "1", "<null>", "<null>"}
        }));

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).contains(
                    new String[]{"2", "Ester", "Colero", "TRUE", "2000-02-01", null},
                    new String[]{"3", "Elca", "Puio", "TRUE", null, null}
            );
        }
    }

    @Test
    public void testInsertFromDataTableWhenEnabledCleanup() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        JsonNode inserted = (JsonNode) contributor.insertFromDataTable(client.table(), new DataTable(new String[][]{
                client.columns(),
                new String[]{"2", "Ester", "Colero", "1", "2000-02-01", "<null>"},
                new String[]{"3", "Elca", "Puio", "1", "<null>", "<null>"}
        }));
        contributor.cleanUp();

        // Check
        assertThat(contributor.cleanUpOperations).hasSize(2);

        Database db = Database.from(contributor.connection());
        String table = db.table("client");
        assertThat(inserted).isInstanceOf(ArrayNode.class);
        assertThat(inserted).isNotEmpty().hasSize(2);
        assertThat(inserted.get(0).get(db.column(table, "id")).asText())
                .isEqualTo("2");
        assertThat(inserted.get(0).get(db.column(table, "first_name")).asText())
                .isEqualTo("Ester");
        assertThat(inserted.get(0).get(db.column(table, "second_name")).asText())
                .isEqualTo("Colero");
        assertThat(inserted.get(0).get(db.column(table, "active")).asText())
                .isEqualTo("TRUE");
        assertThat(inserted.get(0).get(db.column(table, "birth_date")).asText())
                .isEqualTo("2000-02-01");
        assertThat(inserted.get(1).get(db.column(table, "id")).asText())
                .isEqualTo("3");
        assertThat(inserted.get(1).get(db.column(table, "first_name")).asText())
                .isEqualTo("Elca");
        assertThat(inserted.get(1).get(db.column(table, "second_name")).asText())
                .isEqualTo("Puio");
        assertThat(inserted.get(1).get(db.column(table, "active")).asText())
                .isEqualTo("TRUE");
        assertThat(inserted.get(1).get(db.column(table, "birth_date")).asText())
                .isEqualTo("null");

        try (Select<String[]> select = db.select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).doesNotContain(
                    new String[]{"2", "Ester", "Colero", "TRUE", "2000-02-01"},
                    new String[]{"3", "Elca", "Puio", "TRUE", null});
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testInsertFromDataTableWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        contributor.insertFromDataTable("xxxxx", new DataTable(new String[][]{
                client.columns(),
                new String[]{"2", "Ester", "Colero", "1", "2000-02-01"}
        }));

        // Check
        // Exception is thrown
    }

    @Test(expected = SQLRuntimeException.class)
    public void testInsertFromDataTableWhenColumnNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        contributor.insertFromDataTable(client.table(), new DataTable(new String[][]{
                new String[]{"xxx"},
                new String[]{"2"}
        }));

        // Check
        // Exception is thrown
    }

    @Test
    public void testDeleteFromDataTableWhenSingleRow() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        contributor.deleteFromDataTable(client.table(), new DataTable(new String[][]{
                new String[]{"first_name", "second_name", "active"},
                new String[]{"Rosa", "Melano", "1"}
        }));

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void testDeleteFromDataTableWhenSingleRowAndNull() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        contributor.executeSQLScript(new Document("UPDATE client SET birth_date = null WHERE id = 1"));

        Table client = Table.CLIENT;

        // Act
        contributor.deleteFromDataTable(client.table(), new DataTable(new String[][]{
                new String[]{"first_name", "second_name", "active", "birth_date"},
                new String[]{"Rosa", "Melano", "1", "<null>"}
        }));

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void testDeleteFromDataTableWhenEnabledCleanup() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        contributor.deleteFromDataTable(client.table(), new DataTable(new String[][]{
                new String[]{"first_name", "second_name", "active"},
                new String[]{"Rosa", "Melano", "1"}
        }));
        contributor.cleanUp();

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).containsExactly(
                    new String[]{"1", "Rosa", "Melano", "TRUE", "1980-12-25", "2024-07-22 12:34:56.000"}
            );
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testDeleteFromDataTableWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.deleteFromDataTable("xxxxx", new DataTable(new String[][]{
                new String[]{"first_name", "second_name", "active"},
                new String[]{"Rosa", "Melano", "1"}
        }));

        // Check
        // Exception is thrown
    }

    @Test(expected = SQLRuntimeException.class)
    public void testDeleteFromDataTableWhenColumnNotExist() throws IOException {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        contributor.deleteFromDataTable(client.table(), new DataTable(new String[][]{
                new String[]{"xxx"},
                new String[]{"2"}
        }));

        // Check
        // Exception is thrown
    }

    @Test
    public void testClearTable() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        contributor.clearTable(client.table());

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void testClearTableWhenEnabledCleanup() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        contributor.clearTable(client.table());
        contributor.cleanUp();

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).containsExactly(
                    new String[]{"1", "Rosa", "Melano", "TRUE", "1980-12-25", "2024-07-22 12:34:56.000"}
            );
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testClearTableWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.clearTable("xxxxx");

        // Check
        // Exception is thrown
    }

    @Test
    public void testClearTableByClause() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        contributor.clearTableByClause(client.table(), new Document("first_name = 'Rosa' AND active = 1"));

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void testClearTableByClauseWhenEnabledCleanup() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        Table client = Table.CLIENT;

        // Act
        contributor.clearTableByClause(client.table(), new Document("first_name = 'Rosa' AND active = 1"));
        contributor.cleanUp();

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).containsExactly(
                    new String[]{"1", "Rosa", "Melano", "TRUE", "1980-12-25", "2024-07-22 12:34:56.000"}
            );
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testClearTableByClauseWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.clearTableByClause("xxxxx", new Document("first_name = 'Rosa' AND active = 1"));

        // Check
        // Exception is thrown
    }

    @Test(expected = SQLRuntimeException.class)
    public void testClearTableByClauseWhenColumnNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.clearTableByClause("client", new Document("xxxx = 'Rosa'"));

        // Check
        // Exception is thrown
    }

    @Test
    public void testExecuteSQLScript() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        String script =
                "INSERT INTO client (id, first_name, second_name, birth_date) VALUES (2, 'Ester', 'Colero', '2000-01-02');" +
                        "INSERT INTO city (id, name, latitude, longitude) " +
                        "SELECT 2, 'Madrid', 40.416775, -3.703790;" +
                        "UPDATE client SET active = 0 WHERE id = 1;" +
                        "INSERT INTO client_city (clientid, cityid) VALUES (2, 2);" +
                        "INSERT INTO client_city (clientid, cityid) VALUES (2, 1);";
        contributor.executeSQLScript(new Document(script));

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).contains(
                    new String[]{"1", "Rosa", "Melano", "FALSE", "1980-12-25", "2024-07-22 12:34:56.000"},
                    new String[]{"2", "Ester", "Colero", "FALSE", "2000-01-02", null});
        }
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM city").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).containsExactly(
                    new String[]{"1", "Valencia", "39.469906", "-0.376288"},
                    new String[]{"2", "Madrid", "40.416775", "-3.703790"});
        }
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client_city").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).containsExactlyInAnyOrder(
                    new String[]{"1", "1"},
                    new String[]{"2", "2"},
                    new String[]{"2", "1"}
            );
        }
    }

    @Test
    public void testExecuteSQLScriptWhenEnabledCleanup() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        String script =
                "INSERT INTO client (id, first_name, second_name, birth_date) VALUES (2, 'Ester', 'Colero', '2000-01-02');" +
                        "INSERT INTO city (id, name, latitude, longitude) " +
                        "SELECT 2, 'Madrid', 40.416775, -3.703790;" +
                        "UPDATE client SET active = 0 WHERE id = 1;" +
                        "INSERT INTO client_city (clientid, cityid) VALUES (2, 2);" +
                        "INSERT INTO client_city (clientid, cityid) VALUES (2, 1);";
        contributor.executeSQLScript(new Document(script));
        contributor.cleanUp();

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).containsExactly(
                    new String[]{"1", "Rosa", "Melano", "TRUE", "1980-12-25", "2024-07-22 12:34:56.000"}
            );
        }
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM city").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).containsExactly(
                    new String[]{"1", "Valencia", "39.469906", "-0.376288"}
            );
        }
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM client_city").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).containsExactly(
                    new String[]{"1", "1"}
            );
        }
    }

    @Test
    public void testExecuteSQLScriptWhenEnabledCleanupAndInsertWithNoId() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        String script = "INSERT INTO other (something) VALUES (37)";
        contributor.executeSQLScript(new Document(script));
        contributor.cleanUp();

        // Check
        assertThatNoException();
    }

    @Test
    public void testExecuteSQLScriptWhenEnabledCleanupAndUpdateWithNoId() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        String script = "UPDATE other SET something = 37 WHERE something = 47";
        contributor.executeSQLScript(new Document(script));
        contributor.cleanUp();

        // Check
        assertThatNoException();
    }

    @Test
    public void testExecuteSQLScriptWhenEnabledCleanupAndDeleteWithNoId() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        String script = "DELETE FROM other WHERE something = 47";
        contributor.executeSQLScript(new Document(script));
        contributor.cleanUp();

        // Check
        try (Select<String[]> select = Database.from(contributor.connection())
                .select("SELECT * FROM other").get(DatabaseHelper::format)) {
            List<String[]> result = select.stream().collect(Collectors.toList());
            assertThat(result).isNotEmpty();
            assertThat(result).containsExactly(
                    new String[]{"47"}
            );
        }
    }

    @Test
    public void testAssertRowExistsBySingleId() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowExistsBySingleId("1", "client");

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowExistsBySingleIdWhenNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowExistsBySingleId("2", "client");

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=2} exist in table %s, but it doesn't",
                            db.column(table, "id"), table));
            throw new WakamitiException();
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowExistsBySingleIdWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowExistsBySingleId("1", "xxxx");

        // Check
        // Exception is thrown
    }

    @Test
    public void testAssertRowExistsBySingleIdAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowExistsBySingleIdAsync("1", "client", Duration.ofSeconds(1));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowExistsBySingleIdAsyncWhenNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowExistsBySingleIdAsync("2", "client", Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=2} exist in table %s, but it doesn't",
                            db.column(table, "id"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertRowNotExistsBySingleId() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowNotExistsBySingleId("2", "client");

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowNotExistsBySingleIdWhenExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowNotExistsBySingleId("1", "client");

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected no record satisfying {%s=1} exist in table %s, but it does",
                            db.column(table, "id"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertRowNotExistsBySingleIdAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowNotExistsBySingleIdAsync("2", "client", Duration.ofSeconds(1));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowNotExistsBySingleIdAsyncWhenExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowNotExistsBySingleIdAsync("1", "client", Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected no record satisfying {%s=1} exist in table %s, but it does",
                            db.column(table, "id"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertRowExistsByOneColumn() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowExistsByOneColumn("second_name", "Melano", "client");

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowExistsByOneColumnWhenNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowExistsByOneColumn("second_name", "Otro", "client");

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=Otro} exist in table %s, but it doesn't",
                            db.column(table, "second_name"), table));
            throw new WakamitiException();
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowExistsByOneColumnWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowExistsByOneColumn("second_name", "Melano", "xxxx");

        // Check
        // Exception is thrown
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowExistsByOneColumnWhenColumnNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowExistsByOneColumn("xxxx", "Melano", "client");

        // Check
        // Exception is thrown
    }

    @Test
    public void testAssertRowExistsByOneColumnAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowExistsByOneColumnAsync("second_name", "Melano", "client", Duration.ofSeconds(1));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowExistsByOneColumnAsyncWhenNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowExistsByOneColumnAsync("second_name", "Otro", "client", Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=Otro} exist in table %s, but it doesn't",
                            db.column(table, "second_name"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertRowNotExistsByOneColumn() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowNotExistsByOneColumn("second_name", "Otro", "client");

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowNotExistsByOneColumnWhenExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowNotExistsByOneColumn("second_name", "Melano", "client");

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected no record satisfying {%s=Melano} exist in table %s, but it does",
                            db.column(table, "second_name"), table));
            throw new WakamitiException();
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowNotExistsByOneColumnWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowNotExistsByOneColumn("second_name", "Otro", "xxxx");

        // Check
        // Exception is thrown
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowNotExistsByOneColumnWhenColumnNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowNotExistsByOneColumn("xxxx", "Otro", "client");

        // Check
        // Exception is thrown
    }

    @Test
    public void testAssertRowNotExistsByOneColumnAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowNotExistsByOneColumnAsync("second_name", "Otro", "client", Duration.ofSeconds(1));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowNotExistsByOneColumnAsyncWhenExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowNotExistsByOneColumnAsync("second_name", "Melano", "client", Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected no record satisfying {%s=Melano} exist in table %s, but it does",
                            db.column(table, "second_name"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertRowCountByOneColumn() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowCountByOneColumn("second_name", "Melano", "client",
                new MatcherAssertion<>(comparesEqualTo(1L)));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowCountByOneColumnWhenNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowCountByOneColumn("second_name", "Otro", "client",
                    new MatcherAssertion<>(comparesEqualTo(1L)));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=Otro} exist in table %s, but <0L> was less than <1L>",
                            db.column(table, "second_name"), table));
            throw new WakamitiException();
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowCountByOneColumnWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowCountByOneColumn("xxxx", "second_name", "Melano",
                new MatcherAssertion<>(comparesEqualTo(1L)));

        // Check
        // Exception is thrown
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowCountByOneColumnWhenColumnNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowCountByOneColumn("client", "xxxx", "Melano",
                new MatcherAssertion<>(comparesEqualTo(1L)));

        // Check
        // Exception is thrown
    }

    @Test
    public void testAssertRowCountByOneColumnAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowCountByOneColumnAsync("second_name", "Melano", "client",
                new MatcherAssertion<>(comparesEqualTo(1L)), Duration.ofSeconds(1));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowCountByOneColumnAsyncWhenNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowCountByOneColumnAsync("second_name", "Otro", "client",
                    new MatcherAssertion<>(comparesEqualTo(1L)), Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=Otro} exist in table %s, but <0L> was less than <1L>",
                            db.column(table, "second_name"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertRowExistsByClause() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowExistsByClause("client", new Document("birth_date > '1980-12-20'"));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowExistsByClauseWhenNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowExistsByClause("client", new Document("birth_date > '1980-12-30'"));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying the given WHERE clause exist in table %s, but it doesn't",
                            db.table("client")));
            throw new WakamitiException();
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowExistsByClauseWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowExistsByClause("xxxx", new Document("birth_date > '1980-12-20'"));

        // Check
        // Exception is thrown
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowExistsByClauseWhenColumnNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowExistsByClause("client", new Document("xxxx > '1980-12-20'"));

        // Check
        // Exception is thrown
    }

    @Test
    public void testAssertRowExistsByClauseAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowExistsByClauseAsync("client", Duration.ofSeconds(1),
                new Document("birth_date > '1980-12-20'"));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowExistsByClauseAsyncWhenNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowExistsByClauseAsync("client", Duration.ofSeconds(1),
                    new Document("birth_date > '1980-12-30'"));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying the given WHERE clause exist in table %s, but it doesn't",
                            db.table("client")));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertRowNotExistsByClause() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowNotExistsByClause("client", new Document("birth_date > '1980-12-30'"));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowNotExistsByClauseWhenNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowNotExistsByClause("client", new Document("birth_date > '1980-12-20'"));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected no record satisfying the given WHERE clause exist in table %s, but it does",
                            db.table("client")));
            throw new WakamitiException();
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowNotExistsByClauseWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowNotExistsByClause("xxxx", new Document("birth_date > '1980-12-30'"));

        // Check
        // Exception is thrown
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowNotExistsByClauseWhenColumnNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowNotExistsByClause("client", new Document("xxxx > '1980-12-30'"));

        // Check
        // Exception is thrown
    }

    @Test
    public void testAssertRowNotExistsByClauseAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowNotExistsByClauseAsync("client", Duration.ofSeconds(1),
                new Document("birth_date > '1980-12-30'"));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowNotExistsByClauseAsyncWhenNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowNotExistsByClauseAsync("client", Duration.ofSeconds(1),
                    new Document("birth_date > '1980-12-20'"));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected no record satisfying the given WHERE clause exist in table %s, but it does",
                            db.table("client")));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertRowCountByClause() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowCountByClause("client", new MatcherAssertion<>(comparesEqualTo(1L)),
                new Document("birth_date > '1980-12-20'"));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowCountByClauseWhenNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowCountByClause("client", new MatcherAssertion<>(comparesEqualTo(1L)),
                    new Document("birth_date > '1980-12-30'"));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying the given WHERE clause exist in table %s, but <0L> was less than <1L>",
                            db.table("client")));
            throw new WakamitiException();
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowCountByClauseWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowCountByClause("xxxx", new MatcherAssertion<>(comparesEqualTo(1L)),
                new Document("birth_date > '1980-12-20'"));

        // Check
        // Exception is thrown
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertRowCountByClauseWhenColumnNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowCountByClause("client", new MatcherAssertion<>(comparesEqualTo(1L)),
                new Document("xxxx > '1980-12-20'"));

        // Check
        // Exception is thrown
    }

    @Test
    public void testAssertRowCountByClauseAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertRowCountByClauseAsync("client", new MatcherAssertion<>(comparesEqualTo(1L)),
                Duration.ofSeconds(1), new Document("birth_date > '1980-12-20'"));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertRowCountByClauseAsyncWhenNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertRowCountByClauseAsync("client", new MatcherAssertion<>(comparesEqualTo(1L)),
                    Duration.ofSeconds(1), new Document("birth_date > '1980-12-30'"));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying the given WHERE clause exist in table %s, but <0L> was less than <1L>",
                            db.table("client")));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertDataTableExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertDataTableExists("client", new DataTable(new String[][]{
                new String[]{"first_name", "second_name", "active", "birth_date", "creation"},
                new String[]{"Rosa", "Melano", "1", "1980-12-25", "2024-07-22 12:34:56"}
        }));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertDataTableExistsWhenNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);

        try {
            // Act
            contributor.assertDataTableExists("client", new DataTable(new String[][]{
                    new String[]{"first_name", "second_name", "active", "birth_date"},
                    new String[]{"Rosa", "Melano", "0", "1980-12-25"}
            }));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format("[The closest record] " + System.lineSeparator() +
                                    "Expecting actual:" + System.lineSeparator() +
                                    "  {\"%3$s\"=\"TRUE\", \"%4$s\"=\"1980-12-25\", \"%1$s\"=\"Rosa\", \"%2$s\"=\"Melano\"}" + System.lineSeparator() +
                                    "to contain exactly (and in same order):" + System.lineSeparator() +
                                    "  [\"%1$s\"=\"Rosa\"," + System.lineSeparator() +
                                    "    \"%2$s\"=\"Melano\"," + System.lineSeparator() +
                                    "    \"%3$s\"=\"false\"," + System.lineSeparator() +
                                    "    \"%4$s\"=\"1980-12-25\"]" + System.lineSeparator() +
                                    "but some elements were not found:" + System.lineSeparator() +
                                    "  [\"%3$s\"=\"false\"]" + System.lineSeparator() +
                                    "and others were not expected:" + System.lineSeparator() +
                                    "  [\"%3$s\"=\"TRUE\"]" + System.lineSeparator(),
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date")));
            throw new WakamitiException();
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertDataTableExistsWhenNotExistAndNoSimilar() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertDataTableExists("client", new DataTable(new String[][]{
                    new String[]{"first_name", "second_name", "active", "birth_date"},
                    new String[]{"Eva", "Perez", "1", "1980-12-25"}
            }));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=Eva, %s=Perez, %s=true, %s=1980-12-25} exist in table %s, but it doesn't",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"), table));
            throw new WakamitiException();
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertDataTableExistsWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertDataTableExists("xxxx", new DataTable(new String[][]{
                new String[]{"first_name", "second_name", "active", "birth_date"},
                new String[]{"Rosa", "Melano", "1", "1980-12-25"}
        }));

        // Check
        // Exception is thrown
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertDataTableExistsWhenColumnNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertDataTableExists("client", new DataTable(new String[][]{
                new String[]{"xxxx", "second_name", "active", "birth_date"},
                new String[]{"Rosa", "Melano", "1", "1980-12-25"}
        }));

        // Check
        // Exception is thrown
    }

    @Test
    public void testAssertDataTableExistsAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertDataTableExistsAsync("client", Duration.ofSeconds(1), new DataTable(new String[][]{
                new String[]{"first_name", "second_name", "active", "birth_date"},
                new String[]{"Rosa", "Melano", "1", "1980-12-25"}
        }));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertDataTableExistsAsyncWhenNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertDataTableExistsAsync("client", Duration.ofSeconds(1), new DataTable(new String[][]{
                    new String[]{"first_name", "second_name", "active", "birth_date"},
                    new String[]{"Rosa", "Melano", "0", "1980-12-25"}
            }));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "[The closest record] " + System.lineSeparator() +
                                    "Expecting actual:" + System.lineSeparator() +
                                    "  {\"%3$s\"=\"TRUE\", \"%4$s\"=\"1980-12-25\", \"%1$s\"=\"Rosa\", \"%2$s\"=\"Melano\"}" + System.lineSeparator() +
                                    "to contain exactly (and in same order):" + System.lineSeparator() +
                                    "  [\"%1$s\"=\"Rosa\"," + System.lineSeparator() +
                                    "    \"%2$s\"=\"Melano\"," + System.lineSeparator() +
                                    "    \"%3$s\"=\"false\"," + System.lineSeparator() +
                                    "    \"%4$s\"=\"1980-12-25\"]" + System.lineSeparator() +
                                    "but some elements were not found:" + System.lineSeparator() +
                                    "  [\"%3$s\"=\"false\"]" + System.lineSeparator() +
                                    "and others were not expected:" + System.lineSeparator() +
                                    "  [\"%3$s\"=\"TRUE\"]" + System.lineSeparator(),
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date")));
            throw new WakamitiException();
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertDataTableExistsAsyncWhenNotExistsAndNoSimilar() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertDataTableExistsAsync("client", Duration.ofSeconds(1), new DataTable(new String[][]{
                    new String[]{"first_name", "second_name", "active", "birth_date"},
                    new String[]{"Eva", "Perez", "1", "1980-12-25"}
            }));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=Eva, %s=Perez, %s=true, %s=1980-12-25} exist in table %s, but it doesn't",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertDataTableNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertDataTableNotExists("client", new DataTable(new String[][]{
                new String[]{"first_name", "second_name", "active", "birth_date"},
                new String[]{"Rosa", "Melano", "0", "1980-12-25"}
        }));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertDataTableNotExistsWhenExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertDataTableNotExists("client", new DataTable(new String[][]{
                    new String[]{"first_name", "second_name", "active", "birth_date"},
                    new String[]{"Rosa", "Melano", "1", "1980-12-25"}
            }));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected no record satisfying {%s=Rosa, %s=Melano, %s=true, %s=1980-12-25} exist in table %s, but it does",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertDataTableNotExistsAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertDataTableNotExistsAsync("client", Duration.ofSeconds(1), new DataTable(new String[][]{
                new String[]{"first_name", "second_name", "active", "birth_date"},
                new String[]{"Rosa", "Melano", "0", "1980-12-25"}
        }));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertDataTableNotExistsAsyncWhenExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertDataTableNotExistsAsync("client", Duration.ofSeconds(1), new DataTable(new String[][]{
                    new String[]{"first_name", "second_name", "active", "birth_date"},
                    new String[]{"Rosa", "Melano", "1", "1980-12-25"}
            }));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected no record satisfying {%s=Rosa, %s=Melano, %s=true, %s=1980-12-25} exist in table %s, but it does",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertDataTableCount() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertDataTableCount("client", new MatcherAssertion<>(comparesEqualTo(1L)),
                new DataTable(new String[][]{
                        new String[]{"first_name", "second_name", "active", "birth_date"},
                        new String[]{"Rosa", "Melano", "1", "1980-12-25"}
                }));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertDataTableCountWhenNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertDataTableCount("client", new MatcherAssertion<>(comparesEqualTo(1L)),
                    new DataTable(new String[][]{
                            new String[]{"first_name", "second_name", "active", "birth_date"},
                            new String[]{"Rosa", "Melano", "0", "1980-12-25"}
                    }));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=Rosa, %s=Melano, %s=false, %s=1980-12-25} exist in table %s, but <0L> was less than <1L>",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertDataTableCountAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertDataTableCountAsync("client", new MatcherAssertion<>(comparesEqualTo(1L)),
                Duration.ofSeconds(1), new DataTable(new String[][]{
                        new String[]{"first_name", "second_name", "active", "birth_date"},
                        new String[]{"Rosa", "Melano", "1", "1980-12-25"}
                }));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertDataTableCountAsyncWhenExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertDataTableCountAsync("client", new MatcherAssertion<>(comparesEqualTo(1L)),
                    Duration.ofSeconds(1), new DataTable(new String[][]{
                            new String[]{"first_name", "second_name", "active", "birth_date"},
                            new String[]{"Rosa", "Melano", "0", "1980-12-25"}
                    }));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=Rosa, %s=Melano, %s=false, %s=1980-12-25} exist in table %s, but <0L> was less than <1L>",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertXLSFileExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data1.xlsx");

        // Act
        contributor.assertXLSFileExists(file);

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertXLSFileExistsWhenNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data2.xlsx");

        try {
            // Act
            contributor.assertXLSFileExists(file);

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "[The closest record] " + System.lineSeparator() +
                                    "Expecting actual:" + System.lineSeparator() +
                                    "  {\"%3$s\"=\"TRUE\", \"%4$s\"=\"1980-12-25\", \"%5$s\"=\"2024-07-22 12:34:56.000\", \"%1$s\"=\"Rosa\", \"%2$s\"=\"Melano\"}" + System.lineSeparator() +
                                    "to contain exactly (and in same order):" + System.lineSeparator() +
                                    "  [\"%1$s\"=\"Rosa\"," + System.lineSeparator() +
                                    "    \"%2$s\"=\"Melano\"," + System.lineSeparator() +
                                    "    \"%3$s\"=\"false\"," + System.lineSeparator() +
                                    "    \"%4$s\"=\"1980-12-25\"," + System.lineSeparator() +
                                    "    \"%5$s\"=\"2024-07-22 12:34:56.000\"]" + System.lineSeparator() +
                                    "but some elements were not found:" + System.lineSeparator() +
                                    "  [\"%3$s\"=\"false\"]" + System.lineSeparator() +
                                    "and others were not expected:" + System.lineSeparator() +
                                    "  [\"%3$s\"=\"TRUE\"]" + System.lineSeparator(),
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"),
                            db.column(table, "creation")));
            throw new WakamitiException();
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertXLSFileExistsWhenNotExistAndNoSimilar() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data3.xlsx");

        try {
            // Act
            contributor.assertXLSFileExists(file);

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=Eva, %s=Perez, %s=false, %s=1980-12-25} exist in table %s, but it doesn't",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertXLSFileExistsAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data1.xlsx");

        // Act
        contributor.assertXLSFileExistsAsync(file, Duration.ofSeconds(1));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertXLSFileExistsAsyncWhenNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data2.xlsx");

        try {
            // Act
            contributor.assertXLSFileExistsAsync(file, Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "[The closest record] " + System.lineSeparator() +
                                    "Expecting actual:" + System.lineSeparator() +
                                    "  {\"%3$s\"=\"TRUE\", \"%4$s\"=\"1980-12-25\", \"%5$s\"=\"2024-07-22 12:34:56.000\", \"%1$s\"=\"Rosa\", \"%2$s\"=\"Melano\"}" + System.lineSeparator() +
                                    "to contain exactly (and in same order):" + System.lineSeparator() +
                                    "  [\"%1$s\"=\"Rosa\"," + System.lineSeparator() +
                                    "    \"%2$s\"=\"Melano\"," + System.lineSeparator() +
                                    "    \"%3$s\"=\"false\"," + System.lineSeparator() +
                                    "    \"%4$s\"=\"1980-12-25\"," + System.lineSeparator() +
                                    "    \"%5$s\"=\"2024-07-22 12:34:56.000\"]" + System.lineSeparator() +
                                    "but some elements were not found:" + System.lineSeparator() +
                                    "  [\"%3$s\"=\"false\"]" + System.lineSeparator() +
                                    "and others were not expected:" + System.lineSeparator() +
                                    "  [\"%3$s\"=\"TRUE\"]" + System.lineSeparator(),
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"),
                            db.column(table, "creation")));
            throw new WakamitiException();
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertXLSFileExistsAsyncWhenNotExistsAndNoSimilar() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data3.xlsx");

        try {
            // Act
            contributor.assertXLSFileExistsAsync(file, Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=Eva, %s=Perez, %s=false, %s=1980-12-25} exist in table %s, but it doesn't",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertXLSFileNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data2.xlsx");

        // Act
        contributor.assertXLSFileNotExists(file);

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertXLSFileNotExistsWhenExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data1.xlsx");

        try {
            // Act
            contributor.assertXLSFileNotExists(file);

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected no record satisfying {%s=Rosa, %s=Melano, %s=true, %s=1980-12-25, %s=2024-07-22 12:34:56.000} exist in table %s, but it does",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"),
                            db.column(table, "creation"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertXLSFileNotExistsAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data2.xlsx");

        // Act
        contributor.assertXLSFileNotExistsAsync(file, Duration.ofSeconds(1));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertXLSFileNotExistsAsyncWhenExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data1.xlsx");

        try {
            // Act
            contributor.assertXLSFileNotExistsAsync(file, Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected no record satisfying {%s=Rosa, %s=Melano, %s=true, %s=1980-12-25, %s=2024-07-22 12:34:56.000} exist in table %s, but it does",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"),
                            db.column(table, "creation"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertCSVFileExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data1.csv");

        // Act
        contributor.assertCSVFileExists(file, "client");

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertCSVFileExistsWhenNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data2.csv");

        try {
            // Act
            contributor.assertCSVFileExists(file, "client");

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "[The closest record] " + System.lineSeparator() +
                                    "Expecting actual:" + System.lineSeparator() +
                                    "  {\"%3$s\"=\"TRUE\", \"%4$s\"=\"1980-12-25\", \"%5$s\"=\"2024-07-22 12:34:56.000\", \"%1$s\"=\"Rosa\", \"%2$s\"=\"Melano\"}" + System.lineSeparator() +
                                    "to contain exactly (and in same order):" + System.lineSeparator() +
                                    "  [\"%1$s\"=\"Rosa\"," + System.lineSeparator() +
                                    "    \"%2$s\"=\"Melano\"," + System.lineSeparator() +
                                    "    \"%3$s\"=\"false\"," + System.lineSeparator() +
                                    "    \"%4$s\"=\"1980-12-25\"," + System.lineSeparator() +
                                    "    \"%5$s\"=\"2024-07-22 12:34:56.000\"]" + System.lineSeparator() +
                                    "but some elements were not found:" + System.lineSeparator() +
                                    "  [\"%3$s\"=\"false\"]" + System.lineSeparator() +
                                    "and others were not expected:" + System.lineSeparator() +
                                    "  [\"%3$s\"=\"TRUE\"]" + System.lineSeparator(),
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"),
                            db.column(table, "creation")));
            throw new WakamitiException();
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertCSVFileExistsWhenNotExistAndNoSimilar() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data3.csv");

        try {
            // Act
            contributor.assertCSVFileExists(file, "client");

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=Eva, %s=Perez, %s=false, %s=1980-12-25} exist in table %s, but it doesn't",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertCSVFileExistsAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data1.csv");

        // Act
        contributor.assertCSVFileExistsAsync(file, "client", Duration.ofSeconds(1));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertCSVFileExistsAsyncWhenNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data2.csv");

        try {
            // Act
            contributor.assertCSVFileExistsAsync(file, "client", Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format("[The closest record] " + System.lineSeparator() +
                                    "Expecting actual:" + System.lineSeparator() +
                                    "  {\"%3$s\"=\"TRUE\", \"%4$s\"=\"1980-12-25\", \"%5$s\"=\"2024-07-22 12:34:56.000\", \"%1$s\"=\"Rosa\", \"%2$s\"=\"Melano\"}" + System.lineSeparator() +
                                    "to contain exactly (and in same order):" + System.lineSeparator() +
                                    "  [\"%1$s\"=\"Rosa\"," + System.lineSeparator() +
                                    "    \"%2$s\"=\"Melano\"," + System.lineSeparator() +
                                    "    \"%3$s\"=\"false\"," + System.lineSeparator() +
                                    "    \"%4$s\"=\"1980-12-25\"," + System.lineSeparator() +
                                    "    \"%5$s\"=\"2024-07-22 12:34:56.000\"]" + System.lineSeparator() +
                                    "but some elements were not found:" + System.lineSeparator() +
                                    "  [\"%3$s\"=\"false\"]" + System.lineSeparator() +
                                    "and others were not expected:" + System.lineSeparator() +
                                    "  [\"%3$s\"=\"TRUE\"]" + System.lineSeparator(),
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"),
                            db.column(table, "creation")));
            throw new WakamitiException();
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertCSVFileExistsAsyncWhenNotExistsAndNoSimilar() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data3.csv");

        try {
            // Act
            contributor.assertCSVFileExistsAsync(file, "client", Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record satisfying {%s=Eva, %s=Perez, %s=false, %s=1980-12-25} exist in table %s, but it doesn't",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertCSVFileNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data2.csv");

        // Act
        contributor.assertCSVFileNotExists(file, "client");

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertCSVFileNotExistsWhenExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data1.csv");

        try {
            // Act
            contributor.assertCSVFileNotExists(file, "client");

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected no record satisfying {%s=Rosa, %s=Melano, %s=true, %s=1980-12-25, %s=2024-07-22 12:34:56.000} exist in table %s, but it does",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"),
                            db.column(table, "creation"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertCSVFileNotExistsAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data2.csv");

        // Act
        contributor.assertCSVFileNotExistsAsync(file, "client", Duration.ofSeconds(1));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertCSVFileNotExistsAsyncWhenExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        File file = resource("data1.csv");

        try {
            // Act
            contributor.assertCSVFileNotExistsAsync(file, "client", Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            String table = db.table("client");
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected no record satisfying {%s=Rosa, %s=Melano, %s=true, %s=1980-12-25, %s=2024-07-22 12:34:56.000} exist in table %s, but it does",
                            db.column(table, "first_name"),
                            db.column(table, "second_name"),
                            db.column(table, "active"),
                            db.column(table, "birth_date"),
                            db.column(table, "creation"), table));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertTableIsNotEmpty() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertTableIsNotEmpty("client");

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertTableIsNotEmptyWhenNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        contributor.truncateTable("client", false);

        try {
            // Act
            contributor.assertTableIsNotEmpty("client");

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            assertThat(e)
                    .hasMessage(String.format("It was expected some record exist in table %s, but it doesn't",
                            db.table("client")));
            throw new WakamitiException();
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertTableIsNotEmptyWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertTableIsNotEmpty("xxxx");

        // Check
        // Exception is thrown
    }

    @Test
    public void testAssertTableIsNotEmptyAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertTableIsNotEmptyAsync("client", Duration.ofSeconds(1));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertTableIsNotEmptyAsyncWhenNotExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        contributor.truncateTable("client", false);

        try {
            // Act
            contributor.assertTableIsNotEmptyAsync("client", Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            assertThat(e)
                    .hasMessage(String.format(
                            "It was expected some record exist in table %s, but it doesn't",
                            db.table("client")));
            throw new WakamitiException();
        }
    }

    @Test
    public void testAssertTableIsEmpty() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        contributor.truncateTable("client", false);

        // Act
        contributor.assertTableIsEmpty("client");

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertTableIsEmptyWhenExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertTableIsEmpty("client");

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            assertThat(e)
                    .hasMessage(String.format("It was expected no record exist in table %s, but it does",
                            db.table("client")));
            throw new WakamitiException();
        }
    }

    @Test(expected = SQLRuntimeException.class)
    public void testAssertTableIsEmptyWhenTableNotExist() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        // Act
        contributor.assertTableIsEmpty("xxxx");

        // Check
        assertThatNoException();
    }

    @Test
    public void testAssertTableIsEmptyAsync() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);
        contributor.truncateTable("client", false);

        // Act
        contributor.assertTableIsEmptyAsync("client", Duration.ofSeconds(1));

        // Check
        assertThatNoException();
    }

    @Test(expected = WakamitiException.class)
    public void testAssertTableIsEmptyAsyncWhenExists() {
        // Prepare
        Configuration config = configContributor.defaultConfiguration().appendFromPairs(
                "database.connection.url", URL,
                "database.connection.username", USER,
                "database.connection.password", PASS,
                "database.metadata.healthcheck", "false",
                "database.enableCleanupUponCompletion", "true"
        );
        configContributor.configurer().configure(contributor, config);
        createContext(config);

        try {
            // Act
            contributor.assertTableIsEmptyAsync("client", Duration.ofSeconds(1));

            // Check
        } catch (AssertionError e) {
            Database db = Database.from(contributor.connection());
            assertThat(e)
                    .hasMessage(String.format("It was expected no record exist in table %s, but it does",
                            db.table("client")));
            throw new WakamitiException();
        }
    }


    private File resource(String resourceName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(resourceName).getFile());
    }

    private void createContext(Configuration configuration) {
        WakamitiStepRunContext.set(new WakamitiStepRunContext(
                configuration,
                Wakamiti.instance().newBackendFactory().createNonRunnableBackend(configuration),
                Locale.getDefault(),
                Locale.getDefault()
        ));
    }

    private enum Table {

        CLIENT("client", "id", "first_name", "second_name", "active", "birth_date", "creation"),
        CITY("city", "id", "name", "latitude", "longitude"),
        CLIENT_CITY("client_city", "clientid", "cityid");

        private final String table;
        private final String[] columns;

        Table(String table, String... columns) {
            this.table = table;
            this.columns = columns;
        }

        public String table() {
            return table;
        }

        public String[] columns() {
            return columns;
        }
    }
}
