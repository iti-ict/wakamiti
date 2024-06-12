package es.iti.wakamiti.azure;

import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.core.JsonPlanSerializer;
import es.iti.wakamiti.core.Wakamiti;
import imconfig.Configuration;
import org.junit.Test;

import java.io.IOException;

public class TestAzureReporter {

    @Test
    public void test() throws IOException {
        AzureReporter reporter = new AzureReporter();
        Configuration config = Configuration.factory().fromResource("wakamiti.yaml", Thread.currentThread().getContextClassLoader()).inner("wakamiti");
        new AzureConfigContributor().configurer().configure(reporter,config);
        PlanNodeSnapshot result = new JsonPlanSerializer().read(Thread.currentThread().getContextClassLoader().getResourceAsStream("wakamiti.json"));
        reporter.report(result);
    }


}
