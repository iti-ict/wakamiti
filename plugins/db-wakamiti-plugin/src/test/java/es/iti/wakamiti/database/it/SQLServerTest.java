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
import static es.iti.wakamiti.database.jdbc.LogUtils.message;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;


@AnnotatedConfiguration({
        @Property(key = RESOURCE_TYPES, value = "gherkin"),
        @Property(key = RESOURCE_PATH, value = "src/test/resources/wakamiti/features/database-sqlserver.feature"),
        @Property(key = "data.dir", value = "src/test/resources/wakamiti"),
        @Property(key = "database.connection.url", value = "jdbc:sqlserver://localhost:1234;encrypt=false"),
        @Property(key = "database.connection.username", value = "sa"),
        @Property(key = "database.connection.password", value = "$3cr3Tp4s$"),
        @Property(key = DATABASE_HEALTHCHECK, value = "false"),
        @Property(key = DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, value = "false"),
        @Property(key = TREAT_STEPS_AS_TESTS, value = "false")
})
@RunWith(WakamitiJUnitRunner.class)
public class SQLServerTest {

    public static final MSSQLServerContainer<?> CONTAINER = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2017-CU12")
            .acceptLicense()
//            .withDatabaseName("test")
//            .withUsername("user")
            .withPassword("$3cr3Tp4s$")
            .withInitScript("wakamiti/db/create-schema-sqlserver.sql")
            .withCreateContainerCmdModifier(cmd ->
                    cmd.getHostConfig().withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(1234), cmd.getExposedPorts()[0]))
            );

    @BeforeClass
    public static void setUp() throws IOException {
        System.out.println("Creating container. Please, be patient... ");
        TestcontainersWindowsNpipe.startOrSkipOnWindowsNpipeFailure(CONTAINER);
        System.out.println(message("\rContainer [MSSQLServerContainer] started with [url={}, username={}, password={}]",
                CONTAINER.getJdbcUrl(), CONTAINER.getUsername(), CONTAINER.getPassword()));
        String url = CONTAINER.getJdbcUrl();
        String user = CONTAINER.getUsername();
        String password = CONTAINER.getPassword();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(url, user, password));
        File schemaFile = new File(SQLServerTest.class.getResource("/wakamiti/db/triggers.sql").getFile());
        String createSchema = FileUtils.readFileToString(schemaFile, StandardCharsets.UTF_8);
        for (String sentence : createSchema.split("\\sGO\\s")) {
            jdbcTemplate.execute(sentence);
        }
    }

    @AfterClass
    public static void shutdown() {
        CONTAINER.stop();
        CONTAINER.close();
    }

}
