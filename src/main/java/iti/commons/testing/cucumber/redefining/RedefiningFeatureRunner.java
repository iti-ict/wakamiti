/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.cucumber.redefining;

import java.util.ArrayList;
import java.util.List;

import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.junit.ExecutionUnitRunner;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.junit.ScenarioOutlineRunner;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;

public class RedefiningFeatureRunner extends FeatureRunner {

    @SuppressWarnings("rawtypes")
    private List<ParentRunner> children = new ArrayList<>();
    private CucumberFeature cucumberFeature;
    private List<CucumberFeature> features;
    private Runtime runtime;
    private JUnitReporter jUnitReporter;
    private RedefiningOptions options;


    public RedefiningFeatureRunner(
            CucumberFeature cucumberFeature,
            List<CucumberFeature> features,
            Runtime runtime,
            JUnitReporter jUnitReporter,
            RedefiningOptions options
    )
    throws InitializationError {
        super(cucumberFeature, runtime, jUnitReporter);
        this.cucumberFeature = cucumberFeature;
        this.runtime = runtime;
        this.jUnitReporter = jUnitReporter;
        this.options = options;
        this.features = new ArrayList<>(features);
        buildCustomRunners();
    }

    
    private RedefiningContext createRedefiningContext(CucumberTagStatement definition) throws InitializationError {
        return new RedefiningContext(runtime, definition, features, jUnitReporter, options);
    }


    private void buildCustomRunners() {
        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
            if (isScenarioTarget(cucumberTagStatement)) {
                continue;
            }
            try {
                ParentRunner<?> featureElementRunner = null;
                if (isScenarioSource(cucumberTagStatement)) {
                    featureElementRunner = buildRedefinerRunner(cucumberTagStatement);
                } else {
                   featureElementRunner = buildRegularRunner(cucumberTagStatement);
                }

                if (featureElementRunner != null) {
                    children.add(featureElementRunner);
                }

            } catch (InitializationError | RuntimeException e) {
                throw new CucumberException("Error building runner for scenario "+cucumberTagStatement.getVisualName(), e);
            }
        }
    }


    @Override
    public String getName() {
        return getClass().getSimpleName() + super.getName();
    }


    private ParentRunner<?> buildRedefinerRunner(CucumberTagStatement definition) throws InitializationError {
       return RunnerFactory.createRedefinerRunner(createRedefiningContext(definition));
    }



   

    private ParentRunner<?> buildRegularRunner(CucumberTagStatement cucumberTagStatement) throws InitializationError {
        ParentRunner<?> featureElementRunner;
        if (cucumberTagStatement instanceof CucumberScenario) {
            featureElementRunner = new ExecutionUnitRunner(runtime, (CucumberScenario) cucumberTagStatement, jUnitReporter);
        } else {
            featureElementRunner = new ScenarioOutlineRunner(runtime, (CucumberScenarioOutline) cucumberTagStatement, jUnitReporter);
        }
        return featureElementRunner;
    }


    private boolean isScenarioSource(CucumberTagStatement statement) {
        return RunnerFactory.hasSourceTag(statement, options);
    }

    private boolean isScenarioTarget(CucumberTagStatement statement) {
        return RunnerFactory.hasTargetTag(statement, options);
    }



    @SuppressWarnings("rawtypes")
    @Override
    protected List<ParentRunner> getChildren() {
        return children;
    }



}
