/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import iti.wakamiti.api.extensions.ConfigContributor;
import iti.wakamiti.api.extensions.StepContributor;
import iti.wakamiti.database.ConnectionManager;

module wakamiti.db {
    exports iti.wakamiti.database;

    requires wakamiti.api;
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

    provides StepContributor with iti.wakamiti.database.DatabaseStepContributor;
    provides ConfigContributor with iti.wakamiti.database.DatabaseConfigContributor;
    provides ConnectionManager with iti.wakamiti.database.DriverConnectionManager;
}