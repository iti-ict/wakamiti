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
    requires iti.commons.configurer;
    requires org.hamcrest;
    requires junit;
    requires jsqlparser;
    requires org.apache.commons.io;
    requires java.sql.rowset;

    provides StepContributor with iti.kukumo.database.DatabaseStepContributor;
    provides ConfigContributor with iti.kukumo.database.DatabaseConfigContributor;
    provides ConnectionManager with iti.kukumo.database.DriverConnectionManager;
}