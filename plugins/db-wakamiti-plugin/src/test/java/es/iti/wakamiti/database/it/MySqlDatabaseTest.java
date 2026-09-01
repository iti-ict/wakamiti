/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.it;


import static es.iti.wakamiti.api.WakamitiConfiguration.RESOURCE_PATH;
import static es.iti.wakamiti.api.WakamitiConfiguration.RESOURCE_TYPES;
import static es.iti.wakamiti.api.WakamitiConfiguration.TREAT_STEPS_AS_TESTS;
import static es.iti.wakamiti.database.DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION;
import static es.iti.wakamiti.database.DatabaseConfigContributor.DATABASE_HEALTHCHECK;

import java.util.Objects;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;

import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;


@AnnotatedConfiguration({
        @Property(key = RESOURCE_TYPES, value = "gherkin"),
        @Property(key = RESOURCE_PATH, value = "src/test/resources/wakamiti/features/database-mysql.feature"),
        @Property(key = "data.dir", value = "src/test/resources/wakamiti"),
        @Property(key = "database.connection.url", value = "jdbc:mysql://localhost:1234/test"),
        @Property(key = "database.connection.username", value = "user"),
        @Property(key = "database.connection.password", value = "pass"),
        @Property(key = DATABASE_HEALTHCHECK, value = "false"),
        @Property(key = DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, value = "false"),
        @Property(key = TREAT_STEPS_AS_TESTS, value = "false")
})
@RunWith(WakamitiJUnitRunner.class)
public class MySqlDatabaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlDatabaseTest.class);
    private static final MySQLContainer<?> CONTAINER = new MySQLContainer<>("mysql:5.7.34")
            .withDatabaseName("test")
            .withUsername("user")
            .withPassword("pass")
            .withInitScript("wakamiti/db/create-schema.sql")
            .withCreateContainerCmdModifier(cmd ->
                    Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                            new PortBinding(
                                    Ports.Binding.bindPort(1234),
                                    Objects.requireNonNull(cmd.getExposedPorts())[0]
                            ))
            );

    @BeforeClass
    public static void setUp() {
        LOGGER.info("Creating container. Please, be patient... ");
        TestcontainersWindowsNpipe.startOrSkipOnWindowsNpipeFailure(CONTAINER);
        LOGGER.info("Container [MySQLContainer] started with [url={}, username={}, password={}]",
                CONTAINER.getJdbcUrl(), CONTAINER.getUsername(), CONTAINER.getPassword());
    }

    @AfterClass
    public static void shutdown() {
        CONTAINER.close();
    }

}
