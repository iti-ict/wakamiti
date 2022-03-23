/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import iti.kukumo.api.extensions.ConfigContributor;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.database.ConnectionManager;

module kukumo.db {
    exports iti.kukumo.database;

    requires kukumo.core;
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

    provides StepContributor with iti.kukumo.database.DatabaseStepContributor;
    provides ConfigContributor with iti.kukumo.database.DatabaseConfigContributor;
    provides ConnectionManager with iti.kukumo.database.DriverConnectionManager;
}