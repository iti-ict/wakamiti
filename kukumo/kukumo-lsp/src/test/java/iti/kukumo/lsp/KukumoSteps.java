/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.lsp;


import iti.kukumo.api.annotations.I18nResource;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.datatypes.Assertion;
import iti.kukumo.api.extensions.StepContributor;


@I18nResource("test-kukumo-steps")
public class KukumoSteps implements StepContributor {


    @Step(value = "given.set.of.numbers")
    public void setOfNumbers() {
        // nothing
    }


    @Step(value = "simple.step.with.multiple.asserts", args = "integer-assertion")
    public void simpleStepWithMultipleAsserts(Assertion<Integer> a) {
        // nothing
    }

}
