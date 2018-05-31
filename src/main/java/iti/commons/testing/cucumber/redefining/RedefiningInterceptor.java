/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.cucumber.redefining;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.junit.runners.model.InitializationError;

import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import iti.commons.testing.cucumber.interceptor.AbstractCucumberInterceptor;

public class RedefiningInterceptor extends AbstractCucumberInterceptor<RedefiningOptions>{


    private List<CucumberFeature> allFeatures;


    public RedefiningInterceptor(RuntimeOptions runtimeOptions, Annotation options) {
        super(runtimeOptions, options);

        if (options().idTagPattern() == null) {
            throw new IllegalArgumentException("idTagPattern cannot be null");
        }
        if (options().sourceTags() == null || options().sourceTags().length == 0) {
            throw new IllegalArgumentException("definitionTags cannot be null or empty");
        }
        if (options().targetTags() == null || options().targetTags().length == 0) {
            throw new IllegalArgumentException("implementationTags cannot be null or empty");
        }
    }


    @Override
    public void beforeAddChildren(List<CucumberFeature> features) {
        this.allFeatures = new ArrayList<>(features);
    }


     @Override
    public FeatureRunner provideFeatureRunner(CucumberFeature cucumberFeature, Runtime runtime, JUnitReporter jUnitReporter) throws InitializationError {
        boolean allStatementHaveTargetTag = true;
        for (CucumberTagStatement tagStatement : cucumberFeature.getFeatureElements()) {
            allStatementHaveTargetTag = allStatementHaveTargetTag && RunnerFactory.hasTargetTag(tagStatement, options());
        }
        // we cannot redefine any scenario if all of them have the 'target' tag!
        if (allStatementHaveTargetTag) {
            return null;
        }
        return new RedefiningFeatureRunner(cucumberFeature, allFeatures, runtime, jUnitReporter, options());
    }






}
