/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.cucumber.redefining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.junit.ExecutionUnitRunner;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;

public class StepSetRunner extends ExecutionUnitRunner {


    private RedefiningContext context;
    private CucumberStepSet cucumberStepSubset;
    private boolean isFirstStep;
    private boolean isLastStep;

    private Description description;
    private final Map<Step, Description> stepDescriptions = new HashMap<>();
    private final List<Step> runnerSteps = new ArrayList<>();


    public StepSetRunner(
            RedefiningContext context,
            CucumberScenario sourceScenario,
            CucumberStepSet stepSubset,
            boolean isFirstStep,
            boolean isLastStep
    ) throws InitializationError {
        super(context.runtime(), sourceScenario, context.reporter());
        this.context = context;
        this.cucumberStepSubset = stepSubset;
        this.isFirstStep = isFirstStep;
        this.isLastStep = isLastStep;
    }


    @Override
    public List<Step> getRunnerSteps() {
        return runnerSteps;
    }

    @Override
    protected List<Step> getChildren() {
        return cucumberStepSubset.getSteps();
    }

    @Override
    public String getName() {
        String name = cucumberStepSubset.getVisualName();
        return context.makeNameFilenameCompatible(name);
    }


    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), cucumberStepSubset.getGherkinModel());

            if (isFirstStep() && cucumberStepSubset.getCucumberBackground() != null) {
                for (Step backgroundStep : cucumberStepSubset.getCucumberBackground().getSteps()) {
                    // We need to make a copy of that step, so we have a unique one per scenario
                    Step copy = new Step(
                            backgroundStep.getComments(),
                            backgroundStep.getKeyword(),
                            backgroundStep.getName(),
                            backgroundStep.getLine(),
                            backgroundStep.getRows(),
                            backgroundStep.getDocString()
                    );
                    description.addChild(describeChild(copy));
                    runnerSteps.add(copy);
                }
            }

            for (Step step : getChildren()) {
                description.addChild(describeChild(step));
                runnerSteps.add(step);
            }
        }
        return description;
    }



    @Override
    protected Description describeChild(Step child) {
        return stepDescriptions.computeIfAbsent(child, step->{
            String testName = context.makeNameFilenameCompatible(step.getKeyword() + step.getName());
            return Description.createTestDescription(getName(), "=> "+testName, step);
        });
    }



    @Override
    public void run(final RunNotifier notifier) {
        context.reporter().startExecutionUnit(this, notifier);
        cucumberStepSubset.run(context.reporter(), context.reporter(), context.runtime());
        context.reporter().finishExecutionUnit();
    }


    protected boolean isFirstStep() {
        return isFirstStep;
    }

    protected boolean isLastStep() {
        return isLastStep;
    }

}
