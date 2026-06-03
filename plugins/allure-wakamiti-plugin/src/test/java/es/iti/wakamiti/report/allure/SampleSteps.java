package es.iti.wakamiti.report.allure;


import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.extensions.StepContributor;


@I18nResource("sample-steps")
public class SampleSteps implements StepContributor {

    @Step("step.pass")
    public void pass() {
        // no-op
    }

    @Step("step.fail")
    public void fail() {
        throw new AssertionError("Synthetic failure for Allure");
    }

    @Step("step.error")
    public void error() {
        throw new IllegalStateException("Synthetic error for Allure");
    }
}
