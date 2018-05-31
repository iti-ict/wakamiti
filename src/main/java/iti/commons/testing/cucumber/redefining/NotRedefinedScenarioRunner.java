/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.cucumber.redefining;

import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import cucumber.api.PendingException;
import gherkin.formatter.model.Step;

public class NotRedefinedScenarioRunner extends ParentRunner<Step>{

    private RedefiningContext context;


    public NotRedefinedScenarioRunner(RedefiningContext context) throws InitializationError {
        super(Step.class);
        this.context = context;
    }

    @Override
    protected List<Step> getChildren() {
        return Collections.emptyList();
    }


    @Override
    public String getName() {
        return context.obtainSourceVisualName() + " <not implemented>";
    }


    @Override
    public Description getDescription() {
        return Description.createSuiteDescription(getName(), context.sourceModel());
    }



    @Override
    public void run(final RunNotifier notifier) {
        context.runtime().addError(new PendingException("<not implemented>"));
    }



    @Override
    protected Description describeChild(Step child) {
        // not required
        return null;
    }



    @Override
    protected void runChild(Step child, RunNotifier notifier) {
        // not required
    }

}
