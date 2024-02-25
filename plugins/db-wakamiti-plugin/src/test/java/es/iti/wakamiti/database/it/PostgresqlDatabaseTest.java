/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.it;


import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.core.junit.WakamitiJUnitRunner;
import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.testcontainers.containers.PostgreSQLContainer;

import static es.iti.wakamiti.database.DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION;
import static es.iti.wakamiti.database.jdbc.LogUtils.message;


@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features/database-postgres.feature"),
        @Property(key = "data.dir", value = "src/test/resources"),
        @Property(key = "database.type", value = "postgres"),
        @Property(key = "database.connection.url", value = "jdbc:postgresql://localhost:1234/test"),
        @Property(key = "database.connection.username", value = "user"),
        @Property(key = "database.connection.password", value = "pass"),
        @Property(key = DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, value = "false")
})
@RunWith(WakamitiJUnitRunner.class)
public class PostgresqlDatabaseTest {

    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:9.6.12")
            .withDatabaseName("test")
            .withUsername("user")
            .withPassword("pass")
            .withInitScript("db/create-schema-postgres.sql")
            .withCreateContainerCmdModifier(cmd ->
                    cmd.getHostConfig().withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(1234), cmd.getExposedPorts()[0]))
            );

    @BeforeClass
    public static void setUp() {
        System.out.print("Creating container. Please, be patient... ");
        container.start();
        System.out.println(message("\rContainer [PostgreSQLContainer] started with [url={}, username={}, password={}]",
                container.getJdbcUrl(), container.getUsername(), container.getPassword()));
    }

    @AfterClass
    public static void shutdown() {
        container.stop();
        container.close();
    }

}
