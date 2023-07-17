/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.database.test.dialect;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.core.junit.WakamitiJUnitRunner;
import es.iti.wakamiti.database.DatabaseConfigContributor;
import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import org.junit.runner.RunWith;


@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration({
    @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
    @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features"),
    @Property(key = DatabaseConfigContributor.DATABASE_CONNECTION_URL, value = "jdbc:h2:mem:test;INIT=runscript from 'src/test/resources/db/create-schema.sql'"),
    @Property(key = DatabaseConfigContributor.DATABASE_CONNECTION_USERNAME, value = "sa"),
    @Property(key = DatabaseConfigContributor.DATABASE_CONNECTION_PASSWORD, value = ""),
    @Property(key = DatabaseConfigContributor.DATABASE_METADATA_CASE_SENSITIVITY, value = "upper_cased"),
    @Property(key = DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, value = "false")
})
public class TestH2DbLoader {


}