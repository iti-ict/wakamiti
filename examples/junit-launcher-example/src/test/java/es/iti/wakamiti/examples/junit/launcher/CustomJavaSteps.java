package es.iti.wakamiti.examples.junit.launcher;


import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.junit.Assert;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;


@I18nResource("steps/custom-java-steps")
public class CustomJavaSteps implements StepContributor {

    private static final Logger LOGGER = WakamitiLogger.forClass(CustomJavaSteps.class);

    private final List<Runnable> postExecutionRunners = new LinkedList<>();
    private int number;
    private int result;

    @Step(value = "custom.java.set.number", args = "number:int")
    public void setNumber(Integer number) {
        this.number = number;
    }

    @Step("custom.java.multiply.by.two")
    public Integer multiplyByTwo() {
        this.result = number * 2;
        LOGGER.debug("Result: {}", result);
        return result;
    }

    @Step(value = "custom.java.assert.result", args = "expected:int")
    public void assertResult(Integer expected) {
        Assert.assertEquals(expected.intValue(), result);
    }

    @Step(value = "custom.java.schedule.post.execution", args = "action:text")
    public void schedulePostExecutionAction(String action) {
        postExecutionRunners.add(() -> LOGGER.debug("[custom-java-step][post] {}", action));
    }

    @TearDown(order = 1)
    public void runPostExecutionActions() {
        postExecutionRunners.forEach(Runnable::run);
        postExecutionRunners.clear();
    }
}
