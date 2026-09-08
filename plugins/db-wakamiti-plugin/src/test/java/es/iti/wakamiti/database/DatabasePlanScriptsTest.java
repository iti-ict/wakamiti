/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import static es.iti.wakamiti.api.WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS;
import static es.iti.wakamiti.api.WakamitiConfiguration.OUTPUT_FILE_PATH;
import static es.iti.wakamiti.api.WakamitiConfiguration.RESOURCE_PATH;
import static es.iti.wakamiti.api.WakamitiConfiguration.RESOURCE_TYPES;
import static es.iti.wakamiti.api.WakamitiConfiguration.STOP_EXECUTION_ON_ERROR;
import static es.iti.wakamiti.api.WakamitiConfiguration.WORKING_DIR;
import static es.iti.wakamiti.database.DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION;
import static es.iti.wakamiti.database.DatabaseConfigContributor.DATABASE_HEALTHCHECK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.ConfigurationException;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;


public class DatabasePlanScriptsTest {

    private static final String DATABASE_SCRIPTS_SETUP = "database.scripts.setup";
    private static final String DATABASE_SCRIPTS_TEARDOWN = "database.scripts.teardown";
    private static final String URL = "jdbc:h2:mem:plan-scripts;DB_CLOSE_DELAY=-1";
    private static final String SECOND_URL = "jdbc:h2:mem:plan-scripts-second;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";
    private static final Path RESOURCES = Path.of("src/test/resources").toAbsolutePath();
    private static final Path OUTPUT = Path.of("target/plan-scripts-test.json").toAbsolutePath();

    private static Connection observer;
    private static Connection secondObserver;
    private static boolean setupObserved;
    private static boolean scenarioExecuted;

    private DatabaseStepContributor contributor;
    private DatabaseConfigContributor configContributor;

    @BeforeClass
    public static void setUpDatabase() throws SQLException {
        observer = DriverManager.getConnection(URL, USER, PASS);
        secondObserver = DriverManager.getConnection(SECOND_URL, USER, PASS);
        createStateTable(observer);
        createStateTable(secondObserver);
    }

    @AfterClass
    public static void closeDatabase() throws SQLException {
        observer.close();
        secondObserver.close();
    }

    @Before
    public void setUp() throws SQLException {
        contributor = new DatabaseStepContributor();
        configContributor = new DatabaseConfigContributor();
        setupObserved = false;
        scenarioExecuted = false;
        clearState();
    }

    @After
    public void tearDown() throws SQLException {
        contributor.releaseConnection();
        clearState();
    }

    @Test
    public void executesConfiguredScriptsInPlanOrderWithoutAutomaticCleanup() throws SQLException {
        Configuration configuration = databaseConfiguration(Map.of(
                DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, "true",
                DATABASE_SCRIPTS_SETUP, List.of(script("setup-first.sql"), script("setup-second.sql")),
                DATABASE_SCRIPTS_TEARDOWN, List.of(script("teardown.sql"))
        ));
        configContributor.configurer().configure(contributor, configuration);

        contributor.setUpPlan();
        contributor.cleanUp();

        assertThat(currentState()).isEqualTo("ready");

        contributor.tearDownPlan();
        contributor.releasePlanConnections();

        assertThat(currentState()).isNull();
        assertThat(contributor.connections).isEmpty();
    }

    @Test
    public void setupStopsAfterTheFirstFailedScript() throws SQLException {
        Configuration configuration = databaseConfiguration(Map.of(
                DATABASE_SCRIPTS_SETUP, List.of(
                        script("missing.sql"),
                        script("setup-first.sql")
                )
        ));
        configContributor.configurer().configure(contributor, configuration);

        assertThatThrownBy(contributor::setUpPlan)
                .isInstanceOf(WakamitiException.class)
                .hasMessageContaining("default::setup[0]");

        assertThat(currentState()).isNull();
    }

    @Test
    public void teardownAttemptsEveryScriptBeforeConnectionsAreReleased() throws SQLException {
        Configuration configuration = databaseConfiguration(Map.of(
                DATABASE_SCRIPTS_SETUP, List.of(script("setup-first.sql")),
                DATABASE_SCRIPTS_TEARDOWN, List.of(
                        script("missing-first.sql"),
                        script("teardown.sql"),
                        script("missing-second.sql")
                )
        ));
        configContributor.configurer().configure(contributor, configuration);
        contributor.setUpPlan();

        assertThatThrownBy(contributor::tearDownPlan)
                .isInstanceOf(WakamitiException.class)
                .hasMessageContaining("default::teardown[0]")
                .satisfies(error -> assertThat(error.getSuppressed()).hasSize(1));

        assertThat(currentState()).isNull();
        assertThat(contributor.connections).isNotEmpty();

        contributor.releasePlanConnections();

        assertThat(contributor.connections).isEmpty();
    }

    @Test
    public void ignoresDefaultSetupScriptsWhenNoDefaultConnectionIsConfigured() throws SQLException {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(DATABASE_HEALTHCHECK, "false");
        values.put("database.datasource.first.connection.url", URL);
        values.put("database.datasource.first.connection.username", USER);
        values.put("database.datasource.first.connection.password", PASS);
        values.put(DATABASE_SCRIPTS_SETUP, List.of(script("setup-first.sql")));
        Configuration configuration = configContributor.defaultConfiguration()
                .append(Configuration.factory().fromMap(values));
        configContributor.configurer().configure(contributor, configuration);

        contributor.setUpPlan();

        assertThat(currentState(observer)).isNull();
    }

    @Test
    public void ignoresDefaultTeardownScriptsWhenNoDefaultConnectionIsConfigured() throws SQLException {
        Map<String, Object> values = namedDatasourceConfiguration();
        values.put("database.datasource.first.scripts.setup", List.of(script("setup-first.sql")));
        values.put(DATABASE_SCRIPTS_TEARDOWN, List.of(script("teardown.sql")));
        Configuration configuration = configContributor.defaultConfiguration()
                .append(Configuration.factory().fromMap(values));
        configContributor.configurer().configure(contributor, configuration);

        contributor.setUpPlan();
        contributor.tearDownPlan();

        assertThat(currentState(observer)).isEqualTo("first");
    }

    @Test
    public void rejectsNamedDatasourceWithoutConnectionConfiguration() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(DATABASE_HEALTHCHECK, "false");
        values.put("database.datasource.orphan.scripts.setup", List.of(script("setup-first.sql")));
        Configuration configuration = configContributor.defaultConfiguration()
                .append(Configuration.factory().fromMap(values));

        assertThatThrownBy(() -> configContributor.configurer().configure(contributor, configuration))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("datasource 'orphan'");
    }

    @Test
    public void executesScriptsOnTheirNamedDatasourceAndRestoresTheActiveConnection() throws SQLException {
        Map<String, Object> values = namedDatasourceConfiguration();
        values.put("database.datasource.second.scripts.setup", List.of(
                script("setup-first.sql"),
                script("setup-second.sql")
        ));
        values.put("database.datasource.second.scripts.teardown", List.of(script("teardown.sql")));
        Configuration configuration = configContributor.defaultConfiguration()
                .append(Configuration.factory().fromMap(values));
        configContributor.configurer().configure(contributor, configuration);
        contributor.currentConnection.set("first");

        contributor.setUpPlan();

        assertThat(currentState(observer)).isNull();
        assertThat(currentState(secondObserver)).isEqualTo("ready");
        assertThat(contributor.currentConnection).hasValue("first");

        contributor.tearDownPlan();

        assertThat(currentState(secondObserver)).isNull();
        assertThat(contributor.currentConnection).hasValue("first");
    }

    @Test
    public void combinesDefaultAndNamedScriptGroups() throws SQLException {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(DATABASE_HEALTHCHECK, "false");
        values.put("database.connection.url", URL);
        values.put("database.connection.username", USER);
        values.put("database.connection.password", PASS);
        values.put("database.datasource.second.connection.url", SECOND_URL);
        values.put("database.datasource.second.connection.username", USER);
        values.put("database.datasource.second.connection.password", PASS);
        values.put(DATABASE_SCRIPTS_SETUP, List.of(script("setup-first.sql")));
        values.put("database.datasource.second.scripts.setup", List.of(
                script("setup-first.sql"),
                script("setup-second.sql")
        ));
        values.put(DATABASE_SCRIPTS_TEARDOWN, List.of(script("teardown.sql")));
        values.put("database.datasource.second.scripts.teardown", List.of(script("teardown.sql")));
        Configuration configuration = configContributor.defaultConfiguration()
                .append(Configuration.factory().fromMap(values));
        configContributor.configurer().configure(contributor, configuration);

        contributor.setUpPlan();

        assertThat(currentState(observer)).isEqualTo("first");
        assertThat(currentState(secondObserver)).isEqualTo("ready");

        contributor.tearDownPlan();

        assertThat(currentState(observer)).isNull();
        assertThat(currentState(secondObserver)).isNull();
    }

    @Test
    public void executesDatasourceGroupsInConfigurationOrder() throws SQLException {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(DATABASE_HEALTHCHECK, "false");
        values.put("database.datasource.first.connection.url", URL);
        values.put("database.datasource.first.connection.username", USER);
        values.put("database.datasource.first.connection.password", PASS);
        values.put("database.datasource.second.connection.url", URL);
        values.put("database.datasource.second.connection.username", USER);
        values.put("database.datasource.second.connection.password", PASS);
        values.put("database.datasource.first.scripts.setup", List.of(script("setup-first.sql")));
        values.put("database.datasource.second.scripts.setup", List.of(script("setup-second.sql")));
        Configuration configuration = configContributor.defaultConfiguration()
                .append(Configuration.factory().fromMap(values));
        configContributor.configurer().configure(contributor, configuration);

        contributor.setUpPlan();

        assertThat(currentState()).isEqualTo("ready");
    }

    @Test
    public void reportsTheNestedPropertyWhenANamedScriptFails() {
        Map<String, Object> values = namedDatasourceConfiguration();
        values.put("database.datasource.second.scripts.setup", List.of(script("missing.sql")));
        Configuration configuration = configContributor.defaultConfiguration()
                .append(Configuration.factory().fromMap(values));
        configContributor.configurer().configure(contributor, configuration);

        assertThatThrownBy(contributor::setUpPlan)
                .isInstanceOf(WakamitiException.class)
                .hasMessageContaining("second::setup[0]");
    }

    @Test
    public void usesTheOnlyNamedDatasourceForPlanScripts() throws SQLException {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(DATABASE_HEALTHCHECK, "false");
        values.put("database.datasource.named.connection.url", URL);
        values.put("database.datasource.named.connection.username", USER);
        values.put("database.datasource.named.connection.password", PASS);
        values.put("database.datasource.named.scripts.setup",
                List.of(script("setup-first.sql"), script("setup-second.sql")));
        values.put("database.datasource.named.scripts.teardown", List.of(script("teardown.sql")));
        Configuration configuration = configContributor.defaultConfiguration()
                .append(Configuration.factory().fromMap(values));
        configContributor.configurer().configure(contributor, configuration);

        contributor.setUpPlan();

        assertThat(currentState()).isEqualTo("ready");

        contributor.tearDownPlan();
        contributor.releasePlanConnections();

        assertThat(currentState()).isNull();
    }

    @Test
    public void configuresDefaultAndNamedConnectionsTogether() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(DATABASE_HEALTHCHECK, "false");
        values.put("database.connection.url", URL);
        values.put("database.connection.username", USER);
        values.put("database.connection.password", PASS);
        values.put("database.datasource.named.connection.url", URL);
        values.put("database.datasource.named.connection.username", USER);
        values.put("database.datasource.named.connection.password", PASS);
        Configuration configuration = configContributor.defaultConfiguration()
                .append(Configuration.factory().fromMap(values));

        configContributor.configurer().configure(contributor, configuration);

        assertThat(contributor.connections.keySet()).containsExactlyInAnyOrder("default", "named");
    }

    @Test
    public void planExecutionSurroundsScenariosWithDatabaseScripts() throws SQLException {
        Configuration configuration = planConfiguration("plan.feature", Map.of(
                DATABASE_SCRIPTS_SETUP, List.of("plan-scripts/setup-first.sql", "plan-scripts/setup-second.sql"),
                DATABASE_SCRIPTS_TEARDOWN, List.of("plan-scripts/teardown.sql")
        ));
        Wakamiti wakamiti = Wakamiti.instance();
        PlanNode plan = wakamiti.createPlanFromConfiguration(configuration);

        PlanNode executed = wakamiti.executePlan(plan, configuration);

        assertThat(setupObserved).isTrue();
        assertThat(executed.result()).contains(Result.PASSED);
        assertThat(currentState()).isNull();
    }

    @Test
    public void setupFailureContinuesScenariosWhenStopOnErrorIsDisabled() {
        Configuration configuration = planConfiguration("setup-failure.feature", Map.of(
                DATABASE_SCRIPTS_SETUP, List.of("plan-scripts/missing.sql"),
                STOP_EXECUTION_ON_ERROR, "false"
        ));
        Wakamiti wakamiti = Wakamiti.instance();
        PlanNode plan = wakamiti.createPlanFromConfiguration(configuration);

        PlanNode executed = wakamiti.executePlan(plan, configuration);

        assertThat(scenarioExecuted).isTrue();
        assertThat(executed.result()).contains(Result.ERROR);
    }

    @Test
    public void setupFailureSkipsScenariosWhenStopOnErrorIsEnabled() {
        Configuration configuration = planConfiguration("setup-failure.feature", Map.of(
                DATABASE_SCRIPTS_SETUP, List.of("plan-scripts/missing.sql"),
                STOP_EXECUTION_ON_ERROR, "true"
        ));
        Wakamiti wakamiti = Wakamiti.instance();
        PlanNode plan = wakamiti.createPlanFromConfiguration(configuration);

        PlanNode executed = wakamiti.executePlan(plan, configuration);

        assertThat(scenarioExecuted).isFalse();
        assertThat(executed.result()).contains(Result.ERROR);
    }

    private Configuration databaseConfiguration(
            Map<String, ?> extraValues
    ) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(DATABASE_HEALTHCHECK, "false");
        values.put("database.connection.url", URL);
        values.put("database.connection.username", USER);
        values.put("database.connection.password", PASS);
        values.putAll(extraValues);
        return configContributor.defaultConfiguration().append(Configuration.factory().fromMap(values));
    }

    private Configuration planConfiguration(
            String feature,
            Map<String, ?> extraValues
    ) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(RESOURCE_TYPES, GherkinResourceType.NAME);
        values.put(RESOURCE_PATH, "plan-scripts/" + feature);
        values.put(WORKING_DIR, RESOURCES.toString());
        values.put(OUTPUT_FILE_PATH, OUTPUT.toString());
        values.put(NON_REGISTERED_STEP_PROVIDERS, PlanScriptProbeSteps.class.getName());
        values.put(DATABASE_HEALTHCHECK, "false");
        values.put("database.connection.url", URL);
        values.put("database.connection.username", USER);
        values.put("database.connection.password", PASS);
        values.putAll(extraValues);
        return Wakamiti.defaultConfiguration().append(Configuration.factory().fromMap(values));
    }

    private String script(
            String filename
    ) {
        return RESOURCES.resolve("plan-scripts").resolve(filename).toString();
    }

    private static void clearState() throws SQLException {
        clearState(observer);
        clearState(secondObserver);
    }

    private static String currentState() throws SQLException {
        return currentState(observer);
    }

    private static String currentState(
            Connection connection
    ) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT STATE FROM PLAN_SCRIPT_STATE WHERE ID = 1")) {
            return result.next() ? result.getString(1) : null;
        }
    }

    private static void createStateTable(
            Connection connection
    ) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE PLAN_SCRIPT_STATE (ID INT PRIMARY KEY, STATE VARCHAR(20))");
        }
    }

    private static void clearState(
            Connection connection
    ) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM PLAN_SCRIPT_STATE");
        }
    }

    private Map<String, Object> namedDatasourceConfiguration() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(DATABASE_HEALTHCHECK, "false");
        values.put("database.datasource.first.connection.url", URL);
        values.put("database.datasource.first.connection.username", USER);
        values.put("database.datasource.first.connection.password", PASS);
        values.put("database.datasource.second.connection.url", SECOND_URL);
        values.put("database.datasource.second.connection.username", USER);
        values.put("database.datasource.second.connection.password", PASS);
        return values;
    }

    @I18nResource("plan-scripts/steps")
    public static class PlanScriptProbeSteps implements StepContributor {

        @Step("db.plan.script.probe")
        public void observeSetup() throws SQLException {
            setupObserved = "ready".equals(currentState());
            if (!setupObserved) {
                throw new WakamitiException("Database plan setup was not visible");
            }
        }

        @Step("db.plan.script.execution.probe")
        public void observeScenarioExecution() {
            scenarioExecuted = true;
        }

    }

}
