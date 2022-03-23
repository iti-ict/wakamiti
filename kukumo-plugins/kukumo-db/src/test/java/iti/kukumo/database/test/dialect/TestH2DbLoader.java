/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database.test.dialect;


import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import org.junit.runner.RunWith;


import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.database.DatabaseConfigContributor;
import iti.kukumo.junit.KukumoJUnitRunner;


@RunWith(KukumoJUnitRunner.class)
@AnnotatedConfiguration({
    @Property(key = KukumoConfiguration.RESOURCE_TYPES, value = "gherkin"),
    @Property(key = KukumoConfiguration.RESOURCE_PATH, value = "src/test/resources/features"),
    @Property(key = DatabaseConfigContributor.DATABASE_CONNECTION_URL, value = "jdbc:h2:mem:test;INIT=runscript from 'src/test/resources/db/create-schema.sql'"),
    @Property(key = DatabaseConfigContributor.DATABASE_CONNECTION_USERNAME, value = "sa"),
    @Property(key = DatabaseConfigContributor.DATABASE_CONNECTION_PASSWORD, value = ""),
    @Property(key = DatabaseConfigContributor.DATABASE_METADATA_CASE_SENSITIVITY, value = "upper_cased"),
    @Property(key = DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, value = "false")
})
public class TestH2DbLoader {


}