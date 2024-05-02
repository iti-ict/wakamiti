package es.iti.wakamiti.azure;

import es.iti.wakamiti.api.WakamitiConfiguration;
//import es.iti.wakamiti.core.junit.WakamitiJUnitRunner;
import imconfig.AnnotatedConfiguration;
import imconfig.Property;
//import org.junit.runner.RunWith;


@AnnotatedConfiguration({
    @Property(key = WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS, value = "es.iti.wakamiti.azure.MockSteps"),
    @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
    @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources"),
    @Property(key = AzureConfigContributor.AZURE_HOST, value = "azure-devops.iti.upv.es"),
    @Property(key = AzureConfigContributor.AZURE_ORGANIZATION, value = "ST"),
    @Property(key = AzureConfigContributor.AZURE_PROJECT, value = "ACS"),
    @Property(key = AzureConfigContributor.AZURE_CREDENTIALS_PASSWORD, value = "XXXXXX"),
    @Property(key = AzureConfigContributor.AZURE_API_VERSION, value = "6.0-preview"),
    @Property(key = AzureConfigContributor.AZURE_WORK_ITEM_TEST_CASE_TYPE, value = "Caso de prueba"),
    @Property(key = AzureConfigContributor.AZURE_CREATE_ITEMS_IF_ABSENT, value = "true"),
    @Property(key = AzureConfigContributor.AZURE_TIME_ZONE_ADJUSTMENT, value = "-2"),
    @Property(key = AzureConfigContributor.AZURE_ATTACHMENTS, value = "wakamiti.html"),
    @Property(key = AzureConfigContributor.AZURE_TEST_CASE_PER_FEATURE, value = "false")
})
//@RunWith(WakamitiJUnitRunner.class)
public class TestRunConfig {
}

