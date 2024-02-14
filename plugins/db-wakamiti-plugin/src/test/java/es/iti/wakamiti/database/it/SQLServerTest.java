/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.it;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.core.junit.WakamitiJUnitRunner;
import imconfig.AnnotatedConfiguration;
import imconfig.Configuration;
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

import static es.iti.wakamiti.database.DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION;


@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features/database-sqlserver.feature"),
        @Property(key = "data.dir", value = "src/test/resources"),
        @Property(key = "database.type", value = "sqlserver"),
        @Property(key = DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, value = "false")
})
@RunWith(WakamitiJUnitRunner.class)
public class SQLServerTest {

    private static final MSSQLServerContainer<?> container = new MSSQLServerContainer<>()
            .acceptLicense()
            .withInitScript("db/create-schema-sqlserver.sql");


    @BeforeClass
    public static Configuration setUp(Configuration config) throws IOException {
        System.out.println("Creating container. Please, be patient...");
        container.start();
        String url = container.getJdbcUrl();
        String user = container.getUsername();
        String password = container.getPassword();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(url, user, password));
        File schemaFile = new File(SQLServerTest.class.getResource("/db/triggers.sql").getFile());
        String createSchema = FileUtils.readFileToString(schemaFile, StandardCharsets.UTF_8);
        for (String sentence : createSchema.split(System.lineSeparator() + "GO" + System.lineSeparator())) {
            jdbcTemplate.execute(sentence);
        }
        return config.appendProperty("database.connection.url", url)
                .appendProperty("database.connection.username", user)
                .appendProperty("database.connection.password", password);
    }

    @AfterClass
    public static void shutdown() {
        container.stop();
        container.close();
    }

}
