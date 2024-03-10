/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.it;


import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static es.iti.wakamiti.api.WakamitiConfiguration.*;
import static es.iti.wakamiti.database.DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION;
import static es.iti.wakamiti.database.DatabaseConfigContributor.DATABASE_HEALTHCHECK;
import static es.iti.wakamiti.database.jdbc.LogUtils.message;


@AnnotatedConfiguration({
        @Property(key = RESOURCE_TYPES, value = "gherkin"),
        @Property(key = RESOURCE_PATH, value = "src/test/resources/features/database-sqlserver.feature"),
        @Property(key = "data.dir", value = "src/test/resources"),
        @Property(key = "database.connection.url", value = "jdbc:sqlserver://localhost:1234"),
        @Property(key = "database.connection.username", value = "sa"),
        @Property(key = "database.connection.password", value = "$3cr3Tp4s$"),
        @Property(key = DATABASE_HEALTHCHECK, value = "false"),
        @Property(key = DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, value = "false"),
        @Property(key = TREAT_STEPS_AS_TESTS, value = "true")
})
@RunWith(WakamitiJUnitRunner.class)
public class SQLServerTest {

    private static final MSSQLServerContainer<?> container = new MSSQLServerContainer<>()
            .acceptLicense()
//            .withDatabaseName("test")
//            .withUsername("user")
            .withPassword("$3cr3Tp4s$")
            .withInitScript("db/create-schema-sqlserver.sql")
            .withCreateContainerCmdModifier(cmd ->
                    cmd.getHostConfig().withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(1234), cmd.getExposedPorts()[0]))
            );


    @BeforeClass
    public static void setUp() throws IOException {
        System.out.println("Creating container. Please, be patient... ");
        container.start();
        System.out.println(message("\rContainer [MSSQLServerContainer] started with [url={}, username={}, password={}]",
                container.getJdbcUrl(), container.getUsername(), container.getPassword()));
        String url = container.getJdbcUrl();
        String user = container.getUsername();
        String password = container.getPassword();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(url, user, password));
        File schemaFile = new File(SQLServerTest.class.getResource("/db/triggers.sql").getFile());
        String createSchema = FileUtils.readFileToString(schemaFile, StandardCharsets.UTF_8);
        for (String sentence : createSchema.split("\\sGO\\s")) {
            jdbcTemplate.execute(sentence);
        }
    }

    @AfterClass
    public static void shutdown() {
        container.stop();
        container.close();
    }

}
