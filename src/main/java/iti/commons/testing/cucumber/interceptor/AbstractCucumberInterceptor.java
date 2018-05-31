/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.cucumber.interceptor;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;

public abstract class AbstractCucumberInterceptor<T extends Annotation> implements CucumberInterceptor {

    final T options;
    final RuntimeOptions runtimeOptions;


    @SuppressWarnings("unchecked")
    public AbstractCucumberInterceptor(RuntimeOptions runtimeOptions, Annotation options) {
        this.options = (T) options;
        this.runtimeOptions = runtimeOptions;
    }


    protected T options() {
        return options;
    }

    protected RuntimeOptions runtimeOptions() {
        return runtimeOptions;
    }

    @Override
    public void beforeAddChildren(List<CucumberFeature> features) {

    }

    @Override
    public void afterAddChildren(List<CucumberFeature> features) {

    }

    @Override
    public Description describeChild(FeatureRunner child) {
        return null;
    }

    @Override
    public void beforeRunChild(FeatureRunner child, RunNotifier notifier) {

    }

    @Override
    public void afterRunChild(FeatureRunner child, RunNotifier notifier) {

    }

    @Override
    public FeatureRunner provideFeatureRunner(CucumberFeature feature, Runtime runtime, JUnitReporter reporter)
    throws InitializationError {
        return new FeatureRunner(feature, runtime, reporter);
    }


}
