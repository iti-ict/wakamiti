/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.cucumber.redefining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.model.CucumberBackground;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

public class RunnerFactory {


    private RunnerFactory() {
        //
    }



    public static ParentRunner<?> createRedefinerRunner(RedefiningContext context)
    throws InitializationError {

        if (context.existsTarget()) {
            if (context.isSourceScenario() && context.isTargetScenario()) {
                return createRedefinerCompoundRunner(context);
            }
            if (context.isSourceScenarioOutline() && context.isTargetScenarioOutline()) {
                return createRedefinerOutlineCompoundRunner(context);
            }
        }
        return new NotRedefinedScenarioRunner(context);
    }






    protected static CompoundScenarioRunner createRedefinerCompoundRunner(RedefiningContext context)
    throws InitializationError {

        List<ParentRunner<?>> runners = new ArrayList<>();
        Map<Step, List<Step>> redefinedSteps = 
        		context.computeRedefinedSteps(context.sourceScenario(), context.targetScenario());
        // for each definition step, create a virtual scenario
        List<Step> sourceSteps = context.sourceScenario().getSteps();
        runners.add(buildBackgroundOnlyRunner(
                context,
                context.targetScenario().getCucumberBackground(),
                context.sourceScenario()
                ));
        for (int i=0;i< sourceSteps.size();i++) {
            Step sourceStep = sourceSteps.get(i);
            List<Step> delegatedSteps = redefinedSteps.get(sourceStep);
            boolean isFirstStep = false;
            boolean isLastStep = ( i == sourceSteps.size() - 1 );
            runners.add(buildStepSetRunner(
                    context,
                    context.targetScenario().getCucumberBackground(),
                    context.sourceScenario(),
                    sourceStep,
                    delegatedSteps,
                    isFirstStep,
                    isLastStep
            ));
        }
        return new CompoundScenarioRunner(context, context.sourceScenario().getVisualName(), runners);
    }




    protected static CompoundScenarioRunner createRedefinerOutlineCompoundRunner(RedefiningContext context)
    throws InitializationError {

        List<ParentRunner<?>> runners = new ArrayList<>();

        CucumberExamples sourceExamples = context.sourceScenarioOutline().getCucumberExamplesList().get(0);
        List<ExamplesTableRow> exampleRows = sourceExamples.getExamples().getRows();
        ExamplesTableRow exampleHeader = exampleRows.get(0);
        context.targetScenarioOutline().examples(sourceExamples.getExamples());

        List<CucumberScenario> sourceScenarios = sourceExamples.createExampleScenarios();
        List<CucumberScenario> targetScenarios = context.targetScenarioOutline().getCucumberExamplesList().get(0).createExampleScenarios();

        for (int s=0; s<sourceScenarios.size(); s++) {

            CucumberScenario sourceScenario = sourceScenarios.get(s);
            CucumberScenario targetScenario = targetScenarios.get(s);
            ExamplesTableRow currentExample = exampleRows.get(s+1);
            Map<Step, List<Step>> redefinedSteps = 
            		context.computeRedefinedSteps(sourceScenario,targetScenario,exampleHeader,currentExample);

            CucumberBackground background = targetScenario.getCucumberBackground();

            List<ParentRunner<?>> exampleRunners = new ArrayList<>();
            exampleRunners.add(buildBackgroundOnlyRunner(context,background,sourceScenario));
            List<Step> sourceSteps = sourceScenario.getSteps();

            for (int i=0;i< sourceSteps.size();i++) {
                    Step sourceStep = sourceSteps.get(i);
                    List<Step> delegatedSteps = redefinedSteps.get(sourceStep);
                    boolean isFirstStep = false;
                    boolean isLastStep = ( i == sourceSteps.size() - 1 );
                    exampleRunners.add(buildStepSetRunner(
                            context,
                            background,
                            sourceScenario,
                            sourceStep,
                            delegatedSteps,
                            isFirstStep,
                            isLastStep
                    ));
             }
             String visualName = ((ScenarioOutline)context.sourceScenarioOutline().getGherkinModel()).getName() + sourceScenario.getVisualName();
             runners.add(new CompoundScenarioRunner(context,visualName,exampleRunners));
       }
       return new CompoundScenarioRunner(context, context.sourceStatement().getVisualName(), runners);
    }








    private static ParentRunner<?> buildBackgroundOnlyRunner (
            RedefiningContext context,
            CucumberBackground background,
            CucumberScenario sourceScenario
    ) throws InitializationError {
        Step emptyStep = new Step(Collections.emptyList(), "", "<background>", 0, Collections.emptyList(), null);
        return buildStepSetRunner(
                context,
                background,
                sourceScenario,
                emptyStep,
                Collections.emptyList(),
                true,
                false
        );
    }







    private static ParentRunner<?> buildStepSetRunner(
            RedefiningContext context,
            CucumberBackground background,
            CucumberScenario sourceScenario,
            Step sourceStep,
            List<Step> targetSteps,
            boolean isFirstStep,
            boolean isLastStep
    ) throws InitializationError {

        // create a virtual scenario
        Scenario scenario = new Scenario(
                sourceStep.getComments(),
                context.sourceModel().getTags(),
                sourceStep.getKeyword(),
                sourceStep.getName(),
                sourceStep.getName(),
                sourceStep.getLine(),
                sourceStep.getName()
                );
        CucumberBackground delegatedBackground = background;
        CucumberStepSet cucumberStepSubset = new CucumberStepSet(
                context.sourceFeature(),
                sourceScenario,
                delegatedBackground,
                scenario, isFirstStep, isLastStep);
        targetSteps.forEach(cucumberStepSubset::step);
        return new StepSetRunner(context, sourceScenario, cucumberStepSubset, isFirstStep, isLastStep);
    }








    static boolean hasSameIdOrName (CucumberTagStatement source, CucumberTagStatement check, RedefiningOptions options) {
        return options.idTagPattern() == null ?
                source.getGherkinModel().getName().equals(check.getGherkinModel().getName()) :
                getID(source,options) != null && getID(source,options).equals(getID(check,options));
    }



    public static String getID(CucumberTagStatement statement, RedefiningOptions options) {
        return tagMatch(statement, Pattern.compile(options.idTagPattern()), false);
    }

    public static String getIDWithTag(CucumberTagStatement statement, RedefiningOptions options) {
        return tagMatch(statement, Pattern.compile(options.idTagPattern()), true);
    }





    public static boolean hasSourceTag(CucumberTagStatement statement, RedefiningOptions options) {
        return hasTags(statement, options.sourceTags());
    }

    public static boolean hasTargetTag(CucumberTagStatement statement, RedefiningOptions options) {
        return hasTags(statement, options.targetTags());
    }



    private static boolean hasTags(CucumberTagStatement statement, String[] tagStrings) {
        for (Tag tag : statement.getGherkinModel().getTags()) {
            for (String tagString : tagStrings) {
                if (tagString.equals(tag.getName())) {
                    return true;
                }
            }
         }
         return false;
    }



    private static String tagMatch(CucumberTagStatement statement, Pattern tagPattern, boolean returnFullTag) {
        for (Tag tag : statement.getGherkinModel().getTags()) {
           Matcher matcher = tagPattern.matcher(tag.getName());
           if (matcher.find()) {
               return returnFullTag ? tag.getName().substring(1) : matcher.group(1);
           }
        }
        return null;
    }







}
