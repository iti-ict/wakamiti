/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.cucumber.redefining;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.Runtime;
import gherkin.formatter.model.TagStatement;

public class CompoundScenarioRunner extends ParentRunner<ParentRunner<?>>{

    private List<ParentRunner<?>> runners;
    private String id;
    private RedefiningContext context;
    private TagStatement innerModel;
    private String visualName;


    public CompoundScenarioRunner(RedefiningContext context, String visualName, List<ParentRunner<?>> runners) throws InitializationError {    	
        super(CompoundScenarioRunner.class);
        this.id = context.scenarioIDWithTag();
        this.context = context;
        this.visualName = visualName;
        this.innerModel = context.sourceStatement().getGherkinModel();
        this.runners = new ArrayList<>(runners);
    }


    @Override
    protected List<ParentRunner<?>> getChildren() {
        return runners;
    }


    @Override
    public String getName() {
        String name = (id == null ? "" : "["+id+"] ") + visualName;
        if (context.reporter().useFilenameCompatibleNames()) {
            return makeNameFilenameCompatible(name);
        } else {
            return name;
        }
    }


    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(getName(), innerModel);
        getChildren().forEach(child -> description.addChild(describeChild(child)));
        return description;
    }


    


    private String makeNameFilenameCompatible(String name) {
        return name.replaceAll("[^A-Za-z0-9_]", "_");
    }




    @Override
    protected Description describeChild(ParentRunner<?> child) {
        return child.getDescription();
    }


    @Override
    public void run(RunNotifier notifier) {
    	super.run(notifier);
    }
    

    @Override
    protected void runChild(ParentRunner<?> child, RunNotifier notifier) {
       child.run(notifier);
    }


    public Runtime getRuntime() {
        return context.runtime();
    }

}
