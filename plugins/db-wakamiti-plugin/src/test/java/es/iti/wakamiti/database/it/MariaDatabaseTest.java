/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.it;


import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MariaDBContainer;

import static es.iti.wakamiti.api.WakamitiConfiguration.*;
import static es.iti.wakamiti.database.DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION;
import static es.iti.wakamiti.database.DatabaseConfigContributor.DATABASE_HEALTHCHECK;
import static es.iti.wakamiti.database.jdbc.LogUtils.message;


@AnnotatedConfiguration({
        @Property(key = RESOURCE_TYPES, value = "gherkin"),
        @Property(key = RESOURCE_PATH, value = "src/test/resources/features/database-maria.feature"),
        @Property(key = "data.dir", value = "src/test/resources"),
        @Property(key = "database.connection.url", value = "jdbc:mariadb://localhost:1234/test"),
        @Property(key = "database.connection.username", value = "user"),
        @Property(key = "database.connection.password", value = "pass"),
        @Property(key = DATABASE_HEALTHCHECK, value = "false"),
        @Property(key = DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, value = "false"),
        @Property(key = TREAT_STEPS_AS_TESTS, value = "false")
})
@RunWith(WakamitiJUnitRunner.class)
public class MariaDatabaseTest {

    public static final MariaDBContainer<?> container = new MariaDBContainer<>("mariadb:10.3.6")
            .withDatabaseName("test")
            .withUsername("user")
            .withPassword("pass")
            .withInitScript("db/create-schema.sql")
            .withCreateContainerCmdModifier(cmd ->
                    cmd.getHostConfig().withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(1234), cmd.getExposedPorts()[0]))
            );

    @BeforeClass
    public static void setUp() {
        System.out.println("Creating container. Please, be patient... ");
        container.start();
        System.out.println(message("\rContainer [MariaDBContainer] started with [url={}, username={}, password={}]",
                container.getJdbcUrl(), container.getUsername(), container.getPassword()));
    }

    @AfterClass
    public static void shutdown() {
        container.stop();
        container.close();
    }
}
