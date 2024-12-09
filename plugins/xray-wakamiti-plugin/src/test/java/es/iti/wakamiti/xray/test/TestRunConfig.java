package es.iti.wakamiti.xray.test;

import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import org.junit.runner.RunWith;

import static es.iti.wakamiti.api.WakamitiConfiguration.*;
import static es.iti.wakamiti.xray.XrayConfigContributor.*;

@AnnotatedConfiguration({
        @Property(key = NON_REGISTERED_STEP_PROVIDERS, value = "es.iti.wakamiti.xray.test.MockSteps"),
        @Property(key = RESOURCE_TYPES, value = "gherkin"),
        @Property(key = RESOURCE_PATH, value = "src/test/resources"),
        @Property(key = XRAY_ENABLED, value = "true"),
        @Property(key = XRAY_PROJECT, value = "WAK"),
        @Property(key = XRAY_PLAN, value = ""),
        @Property(key = XRAY_PLAN_ID, value = ""),
        @Property(key = XRAY_PLAN_SUMMARY, value = "Sincronización con plugin Wakamiti"),
        @Property(key = XRAY_SUITE_BASE, value = "features"),
        @Property(key = XRAY_TEST_CASE_PER_FEATURE, value = "false"),
        @Property(key = XRAY_CREATE_ITEMS_IF_ABSENT, value = "true"),
        @Property(key = XRAY_BASE_URL, value = "https://eu.xray.cloud.getxray.app"),
        @Property(key = JIRA_BASE_URL, value = "XXXX"),
        @Property(key = XRAY_CREDENTIALS_CLIENT_ID, value = "XXXX"),
        @Property(key = XRAY_CREDENTIALS_CLIENT_SECRET, value = "XXXX"),
        @Property(key = JIRA_CREDENTIALS, value = "XXXX")
})
@RunWith(WakamitiJUnitRunner.class)
public class TestRunConfig {

}

