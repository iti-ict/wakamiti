/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.azure;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;
import imconfig.AnnotatedConfiguration;
import imconfig.Configuration;
import imconfig.Property;
import org.junit.Test;


@AnnotatedConfiguration({
    @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
    @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/azure.feature"),
    @Property(key = WakamitiConfiguration.OUTPUT_FILE_PATH, value = "target/wakamiti-%execID%.json"),
    @Property(key = WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS, value =  "es.iti.wakamiti.azure.MockSteps"),
    @Property(key = AzureConfigContributor.AZURE_HOST, value = "azure-devops.iti.upv.es"),
    @Property(key = AzureConfigContributor.AZURE_ORGANIZATION, value = "ImasD"),
    @Property(key = AzureConfigContributor.AZURE_PROJECT, value = "SIDI"),
    @Property(key = AzureConfigContributor.AZURE_CREDENTIALS_USER, value = ""),
    @Property(key = AzureConfigContributor.AZURE_CREDENTIALS_PASSWORD, value = "y5h42vhd2yqrn5prgzbpgysqub55jn2q42xjc6cjujsqjjzoadaa"),
    @Property(key = AzureConfigContributor.AZURE_API_VERSION, value = "5.0")
})
public class TestAzure {


    @Test
    public void azureIntegration() {
        Wakamiti wakamiti = Wakamiti.instance();
        Configuration conf = Wakamiti.defaultConfiguration().appendFromAnnotation(TestAzure.class);
        PlanNode plan = wakamiti.createPlanFromConfiguration(conf);
        PlanNodeSnapshot result = new PlanNodeSnapshot(Wakamiti.instance().executePlan(plan,conf));
        AzureReporter reporter = new AzureReporter();
        new AzureConfigContributor().configurer().configure(reporter,conf);
        reporter.report(result);
    }

}