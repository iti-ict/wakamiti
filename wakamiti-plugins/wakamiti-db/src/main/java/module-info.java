/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.database.DatabaseConfigContributor;
import es.iti.wakamiti.database.DatabaseStepContributor;
import es.iti.wakamiti.database.DriverConnectionManager;
import es.iti.wakamiti.database.ConnectionManager;


module es.iti.wakamiti.db {
    exports es.iti.wakamiti.database;
    exports es.iti.wakamiti.database.dataset;

    requires es.iti.wakamiti.api;
    requires java.sql;
    requires poi;
    requires poi.ooxml;
    requires poi.ooxml.schemas;
    requires commons.csv;
    requires org.assertj.core;
    requires org.hamcrest;
    requires junit;
    requires jsqlparser;
    requires org.apache.commons.io;
    requires java.sql.rowset;
    requires slf4jansi;

    provides StepContributor with DatabaseStepContributor;
    provides ConfigContributor with DatabaseConfigContributor;
    provides ConnectionManager with DriverConnectionManager;

}

