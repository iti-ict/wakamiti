/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.cucumber.redefining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.runners.model.InitializationError;

import cucumber.runtime.Runtime;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.TagStatement;

public class RedefiningContext {

    private Runtime runtime;
    private CucumberFeature sourceFeature;
    private CucumberTagStatement sourceScenario;
    private String scenarioID;
    private String scenarioIDWithTag;
    private List<CucumberFeature> features;
    private Map<CucumberTagStatement,CucumberFeature> featureByStatement;
    private JUnitReporter reporter;
    private RedefiningOptions options;
    private CucumberTagStatement targetScenario;



    public RedefiningContext (
            Runtime runtime,
            CucumberTagStatement sourceScenario,
            List<CucumberFeature> features,
            JUnitReporter reporter,
            RedefiningOptions options
    ) throws InitializationError {

        this.runtime = runtime;
        this.sourceScenario = sourceScenario;
        this.features = new ArrayList<>(features);
        this.reporter = reporter;
        this.options = options;
        this.scenarioID = RunnerFactory.getID(sourceScenario, options);
        this.scenarioIDWithTag = RunnerFactory.getIDWithTag(sourceScenario, options);
        this.targetScenario = obtainTargetScenario();
        this.featureByStatement = new HashMap<>();
        for (CucumberFeature feature : features) {
            for (CucumberTagStatement statement : feature.getFeatureElements()) {
                this.featureByStatement.put(statement,feature);
            }
        }
        this.sourceFeature = this.featureByStatement.get(sourceScenario);
    }



    private CucumberTagStatement obtainTargetScenario() throws InitializationError {
        // search the statement with target tag and same id or name
        List<CucumberTagStatement> redefinitions = features.stream()
                .flatMap(feature -> feature.getFeatureElements().stream())
                .filter(statement -> RunnerFactory.hasTargetTag(statement, options))
                .filter(statement -> RunnerFactory.hasSameIdOrName(sourceScenario,statement,options))
                .collect(Collectors.toList());
        if (redefinitions.size() > 1) {
            throw new InitializationError("Scenario with id "+scenarioID()+" has more than one redefinition");
        }
        return redefinitions.isEmpty() ? null : redefinitions.get(0);
    }




    public String scenarioID() {
        return this.scenarioID;
    }

    public String scenarioIDWithTag() {
        return scenarioIDWithTag;
    }


    public String obtainSourceVisualName() {
        String name;
        if (this.scenarioID != null) {
            name = String.format("[%s] %s", scenarioIDWithTag, sourceScenario.getVisualName());
        } else {
            name = sourceScenario.getVisualName();
        }
        return makeNameFilenameCompatible(name);
    }

    public Map<Step,List<Step>> computeRedefinedSteps(
    		CucumberScenario sourceScenario, 
    		CucumberScenario targetScenario 
    ) {
    	return computeRedefinedSteps(sourceScenario, targetScenario, null, null);
    }

    
    
    


    public Map<Step,List<Step>> computeRedefinedSteps(
    		CucumberScenario sourceScenario, 
    		CucumberScenario targetScenario, 
    		ExamplesTableRow exampleHeader, 
    		ExamplesTableRow currentExample
    ) {

        Map<String,Step> sourceStepsByName = sourceScenario.getSteps().stream()
                .collect(Collectors.toMap(this::stepKeywordName, o->o));

        Map<Step,List<Step>> stepMap = new HashMap<>();
        sourceScenario.getSteps().forEach(step -> stepMap.put(step, new ArrayList<>()));

        Step lastSourceStep = null;
        for (Step targetStep : targetScenario.getSteps()) {
            Step backtrackedSourceStep = 
            		backtrackSourceStepFromComments(sourceStepsByName, targetStep, exampleHeader, currentExample);
            if (backtrackedSourceStep != null) {
                lastSourceStep = backtrackedSourceStep;
            }
            if (lastSourceStep != null) {
                stepMap.get(lastSourceStep).add(targetStep);
            }
        }
        return stepMap;
    }




    private Step backtrackSourceStepFromComments(
    		Map<String,Step> sourceStepsByName, 
    		Step targetStep, 
    		ExamplesTableRow exampleHeader, 
    		ExamplesTableRow currentExample
    ) {
        if (!targetStep.getComments().isEmpty()) {
        	int lastCommentIndex = targetStep.getComments().size()-1;
            String lastComment = targetStep.getComments().get(lastCommentIndex).getValue();
            
            if (exampleHeader != null && currentExample != null) {
            	for (int i=0;i<exampleHeader.getCells().size();i++) {
            		String header = exampleHeader.getCells().get(i);
            		String value = String.valueOf( currentExample.getCells().get(i) );
            		lastComment = lastComment.replace("<"+header+">", value);
            	}
            }
            return sourceStepsByName.get(lastComment.replaceFirst("#", "").trim());
        }
        return null;
    }




    private String stepKeywordName(Step step) {
        return (step.getKeyword()+step.getName()).trim();
    }



    public String makeNameFilenameCompatible(String name) {
        String compatibleName = name;
        if (reporter.useFilenameCompatibleNames()) {
            compatibleName = compatibleName.replaceAll("[^A-Za-z0-9_]", "_");
        }
        return compatibleName;
    }





    public boolean existsTarget() {
        return targetScenario != null;
    }

    public boolean isSourceScenarioOutline() {
        return sourceScenario instanceof CucumberScenarioOutline;
    }

    public boolean isSourceScenario() {
        return sourceScenario instanceof CucumberScenario;
    }

    public boolean isTargetScenarioOutline() {
        return targetScenario instanceof CucumberScenarioOutline;
    }

    public boolean isTargetScenario() {
        return targetScenario instanceof CucumberScenario;
    }


    public CucumberScenario sourceScenario() {
        return (CucumberScenario) sourceScenario;
    }

    public CucumberScenarioOutline sourceScenarioOutline() {
        return (CucumberScenarioOutline) sourceScenario;
    }

    public CucumberScenario targetScenario() {
        return (CucumberScenario) targetScenario;
    }

    public CucumberScenarioOutline targetScenarioOutline() {
        return (CucumberScenarioOutline) targetScenario;
    }

    
    public CucumberTagStatement sourceStatement() {
    	return isSourceScenario() ? sourceScenario() : sourceScenarioOutline();
    }

    public TagStatement sourceModel() {
        return sourceScenario.getGherkinModel();
    }

    public Runtime runtime() {
        return this.runtime;
    }

    public CucumberFeature sourceFeature() {
        return this.sourceFeature;
    }

    public JUnitReporter reporter() {
        return reporter;
    }
}