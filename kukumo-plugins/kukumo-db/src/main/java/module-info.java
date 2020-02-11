import iti.kukumo.api.extensions.ConfigContributor;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.database.DatabaseConfigContributor;
import iti.kukumo.database.DatabaseStepContributor;

module kukumo.db {

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

    uses StepContributor;
    uses ConfigContributor;

    provides StepContributor with DatabaseStepContributor;
    provides ConfigContributor with DatabaseConfigContributor;

    exports iti.kukumo.database;
}