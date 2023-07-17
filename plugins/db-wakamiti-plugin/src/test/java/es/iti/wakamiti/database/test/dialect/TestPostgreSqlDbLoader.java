/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.database.test.dialect;

import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.core.junit.WakamitiJUnitRunner;
import es.iti.wakamiti.database.DatabaseConfigContributor;
import imconfig.AnnotatedConfiguration;
import imconfig.Configuration;
import imconfig.Property;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;



@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features"),
        @Property(key = DatabaseConfigContributor.DATABASE_CONNECTION_USERNAME, value = "postgres"),
        @Property(key = DatabaseConfigContributor.DATABASE_CONNECTION_PASSWORD, value = "password"),
        @Property(key = DatabaseConfigContributor.DATABASE_METADATA_CASE_SENSITIVITY, value = "lower_cased"),
        @Property(key = DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, value = "false")
})

@Category(LinuxTests.class)
public class TestPostgreSqlDbLoader {
    @BeforeClass
    public static Configuration setUp(Configuration config) throws IOException {
        GenericContainer postgresContainer = new GenericContainer("postgres:15.2")
                    .withExposedPorts(5432)
//                .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust")
                    .withEnv("POSTGRES_USER", "postgres")
                    .withEnv("POSTGRES_PASSWORD", "password")
                    .withCommand("--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci");

            postgresContainer.start();

            String jdbcUrl = "jdbc:postgresql://host.docker.internal" + ":" + postgresContainer.getMappedPort(5432) + "/";
//        String jdbcUrl = "jdbc:postgresql://" + postgresContainer.getContainerIpAddress() + ":" + postgresContainer.getMappedPort(5432)+"/";
            new JdbcTemplate(new DriverManagerDataSource(jdbcUrl, "root", "password")).execute("CREATE DATABASE mydb");


            JdbcTemplate jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(jdbcUrl + "mydb", "root", "password"));


            File schemaFile = new File(TestPostgreSqlDbLoader.class.getResource("/db/create-schema.sql").getFile());
            String createSchema = FileUtils.readFileToString(schemaFile, StandardCharsets.UTF_8);
            for (String sentence : createSchema.split(";")) {
                jdbcTemplate.execute(sentence);
            }

            return config.appendProperty(DatabaseConfigContributor.DATABASE_CONNECTION_URL, jdbcUrl + "mydb");
    }

}
