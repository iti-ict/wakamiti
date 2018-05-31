/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.cucumber.interceptor;

import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.Runtime;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;

public interface CucumberInterceptor {

    void beforeAddChildren(List<CucumberFeature> features);

    void afterAddChildren(List<CucumberFeature> features);

    Description describeChild(FeatureRunner child);

    void beforeRunChild(FeatureRunner child, RunNotifier notifier);

    void afterRunChild(FeatureRunner child, RunNotifier notifier);

    FeatureRunner provideFeatureRunner(CucumberFeature feature, Runtime runtime, JUnitReporter reporter)
      throws InitializationError;

}
