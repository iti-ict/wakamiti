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
import org.testcontainers.containers.MariaDBContainer;

import static es.iti.wakamiti.database.DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION;


@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features/database-maria.feature"),
        @Property(key = "data.dir", value = "src/test/resources"),
        @Property(key = "database.type", value = "maria"),
        @Property(key = DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, value = "false")
})
@RunWith(WakamitiJUnitRunner.class)
public class MariaDatabaseTest {

    private static final MariaDBContainer<?> container = new MariaDBContainer<>("mariadb:10.3.6")
            .withInitScript("db/create-schema.sql");

    @BeforeClass
    public static Configuration setUp(Configuration config) {
        System.out.println("Creating container. Please, be patient...");
        container.start();
        return config.appendProperty("database.connection.url", container.getJdbcUrl())
                .appendProperty("database.connection.username", container.getUsername())
                .appendProperty("database.connection.password", container.getPassword());
    }

    @AfterClass
    public static void shutdown() {
        container.stop();
        container.close();
    }
}
