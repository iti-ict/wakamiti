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
        @Property(key = XRAY_BASE_URL, value = "https://eu.xray.cloud.getxray.app"),
        @Property(key = JIRA_BASE_URL, value = "https://coliva-jira-1.atlassian.net"),
        @Property(key = XRAY_PROJECT, value = "WAK"),
        @Property(key = XRAY_PLAN, value = ""),
        @Property(key = XRAY_PLAN_ID, value = ""),
        @Property(key = XRAY_PLAN_SUMMARY, value = "Sincronización con plugin Wakamiti"),
        @Property(key = XRAY_SUITE_BASE, value = "features"),
        @Property(key = XRAY_TEST_CASE_PER_FEATURE, value = "false"),
        @Property(key = XRAY_CREATE_ITEMS_IF_ABSENT, value = "true"),
        @Property(key = XRAY_CREDENTIALS_CLIENT_ID, value = "FC02F7622A6D4C61A7964467DF5797B5"),
        @Property(key = XRAY_CREDENTIALS_CLIENT_SECRET, value = "34db0165d6461ce1d6d160fac84249016c44af63c28e4bbe24b602c47c2ec84a"),
        @Property(key = JIRA_CREDENTIALS, value = "Y29saXZhK2ppcmFAaXRpLmVzOkFUQVRUM3hGZkdGMGNyaThicndxa0d0TGxxdXVkUHdSaXZvbUdsdmRxSDJoeTdnMkdsYm4yb1QwY0hCNlV1MFNUUzFQTldMRjlzSElzdU9EWE5ZRy1XNi1pR2NURUY3XzJ0NnJISzBoVkdQMG1zRU1VemZwcEl6NVpCRmxDRUJzQnhJRFRwb0hXY2E3N2pScEVQSW5KOWF3aUNSZlJja2hpU1VJaFZvalNPMl9SbVBrdHpXSW94RT1FQzk3QkQ0OQ==")
})
@RunWith(WakamitiJUnitRunner.class)
public class TestRunConfig {

}

