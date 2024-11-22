package es.iti.wakamiti.xray.test;

import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.core.junit.WakamitiJUnitRunner;
import org.junit.runner.RunWith;

import static es.iti.wakamiti.api.WakamitiConfiguration.*;
import static es.iti.wakamiti.xray.XrayConfigContributor.*;

@AnnotatedConfiguration({
        @Property(key = NON_REGISTERED_STEP_PROVIDERS, value = "es.iti.wakamiti.azure.MockSteps"),
        @Property(key = RESOURCE_TYPES, value = "gherkin"),
        @Property(key = RESOURCE_PATH, value = "src/test/resources"),
        @Property(key = XRAY_ENABLED, value = "true"),
        @Property(key = XRAY_BASE_URL, value = "https://eu.xray.cloud.getxray.app"),
        @Property(key = JIRA_BASE_URL, value = "https://coliva-jira-1.atlassian.net"),
        @Property(key = XRAY_PROJECT, value = "W1"),
        @Property(key = XRAY_PLAN, value = ""),
        @Property(key = XRAY_PLAN_ID, value = "10035"),
        @Property(key = XRAY_PLAN_SUMMARY, value = "Sincronización con plugin Wakamiti"),
        @Property(key = XRAY_TEST_CASE_PER_FEATURE, value = "true"),
        @Property(key = XRAY_CREATE_ITEMS_IF_ABSENT, value = "true"),
        @Property(key = XRAY_CREDENTIALS_CLIENT_ID, value = "50767A593E434541BD78A82431F177AA"),
        @Property(key = XRAY_CREDENTIALS_CLIENT_SECRET, value = "c0cc9f057ab3fc26493b60a133e4b33af016df493081a5356d0ebbb01c3bd672"),
        @Property(key = JIRA_CREDENTIALS, value = "Y29saXZhK2ppcmFAaXRpLmVzOkFUQVRUM3hGZkdGMGNyaThicndxa0d0TGxxdXVkUHdSaXZvbUdsdmRxSDJoeTdnMkdsYm4yb1QwY0hCNlV1MFNUUzFQTldMRjlzSElzdU9EWE5ZRy1XNi1pR2NURUY3XzJ0NnJISzBoVkdQMG1zRU1VemZwcEl6NVpCRmxDRUJzQnhJRFRwb0hXY2E3N2pScEVQSW5KOWF3aUNSZlJja2hpU1VJaFZvalNPMl9SbVBrdHpXSW94RT1FQzk3QkQ0OQ==")
})
@RunWith(WakamitiJUnitRunner.class)
public class TestRunConfig {

}

