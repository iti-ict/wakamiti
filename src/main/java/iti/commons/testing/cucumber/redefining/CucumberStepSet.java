/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.cucumber.redefining;

import static gherkin.util.FixJava.join;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberBackground;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

public class CucumberStepSet extends CucumberScenario {

    private final CucumberFeature originalFeature;
    private final CucumberScenario originalScenario;
    private final CucumberBackground cucumberBackground;
    private final Scenario scenario;
    private final boolean firstStep;
    private final boolean lastStep;
    private final String visualName;

    public CucumberStepSet(CucumberFeature originalFeature, CucumberScenario originalScenario, CucumberBackground cucumberBackground, Scenario scenario, boolean firstStep, boolean lastStep) {
        super(originalFeature, cucumberBackground, scenario);
        this.cucumberBackground = cucumberBackground;
        this.scenario = scenario;
        this.originalFeature = originalFeature;
        this.originalScenario = originalScenario;
        this.firstStep = firstStep;
        this.lastStep = lastStep;
        this.visualName = scenario.getKeyword() + scenario.getName();
    }

    public CucumberStepSet(CucumberFeature originalFeature, CucumberScenario originalScenario, CucumberBackground cucumberBackground, Scenario exampleScenario, Row example, boolean firstStep, boolean lastStep) {
        super(originalFeature, cucumberBackground, exampleScenario, example);
        this.cucumberBackground = cucumberBackground;
        this.scenario = exampleScenario;
        this.originalFeature = originalFeature;
        this.originalScenario = originalScenario;
        this.firstStep = firstStep;
        this.lastStep = lastStep;
        this.visualName = "| " + join(example.getCells(), " | ") + " |";
    }



    @Override
    public String getVisualName() {
        return this.visualName;
    }


    /**
     * This method is called when Cucumber is run from the CLI or JUnit
     */
    @Override
    public void run(Formatter formatter, Reporter reporter, Runtime runtime) {

        Set<Tag> tags = tagsAndInheritedTags();
        if (firstStep) {
            runtime.buildBackendWorlds(reporter, tags, (Scenario)originalScenario.getGherkinModel());
            formatter.startOfScenarioLifeCycle((Scenario)originalScenario.getGherkinModel());
            runtime.runBeforeHooks(reporter, tags);
            runBackground(formatter, reporter, runtime);
        }
        getSteps().forEach(formatter::step);
        getSteps().forEach(step->doRunStep(step,reporter,runtime));
        if (lastStep) {
            runtime.runAfterHooks(reporter, tags);
            formatter.endOfScenarioLifeCycle((Scenario)originalScenario.getGherkinModel());
            runtime.disposeBackendWorlds(createScenarioDesignation());
        }
    }

    private String createScenarioDesignation() {
        return originalFeature.getPath() + ":" + Integer.toString(scenario.getLine()) + " # " +
                scenario.getKeyword() + ": " + scenario.getName();
    }


    private void runBackground(Formatter formatter, Reporter reporter, Runtime runtime) {
        if (cucumberBackground != null) {
            format(cucumberBackground, formatter);
            cucumberBackground.getSteps().forEach(step->doRunStep(step, reporter, runtime));
        }
    }



    void format(CucumberBackground background, Formatter formatter) {
    	formatter.background(recreateBackground(background));
        for (Step step : background.getSteps()) {
            formatter.step(step);
        }
    }



    private Background recreateBackground(CucumberBackground background) {
    	List<Comment> comments = Collections.emptyList();
		Integer line = 1;
		String description = "<background>";
		String name = "background";
		String keyword = "Background";
		return new Background(comments, keyword, name, description, line);
	}

	private void doRunStep(Step step, Reporter reporter, Runtime runtime) {
        runtime.runStep(originalFeature.getPath(), step, reporter, originalFeature.getI18n());
    }
}
