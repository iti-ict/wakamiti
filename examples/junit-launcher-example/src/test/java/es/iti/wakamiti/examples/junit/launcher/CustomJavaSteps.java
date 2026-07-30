/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
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


/**
 * Example step contributor used by launcher integration tests.
 * <p>
 * This class keeps mutable scenario state ({@code number}, {@code result},
 * deferred post-execution actions) and is intended as a demo fixture.
 * </p>
 */
@I18nResource("steps/custom-java-steps")
public class CustomJavaSteps implements StepContributor {

    private static final Logger LOGGER = WakamitiLogger.forClass(CustomJavaSteps.class);

    private final List<Runnable> postExecutionRunners = new LinkedList<>();
    private int number;
    private int result;

    /**
     * Stores the input number used by subsequent calculation steps.
     *
     * @param number source value
     */
    @Step(value = "custom.java.set.number", args = "number:int")
    public void setNumber(
            Integer number
    ) {
        this.number = number;
    }

    /**
     * Multiplies the current number by two and exposes the value as step
     * response.
     *
     * @return multiplication result
     */
    @Step("custom.java.multiply.by.two")
    public Integer multiplyByTwo() {
        this.result = number * 2;
        LOGGER.debug("Result: {}", result);
        return result;
    }

    /**
     * Asserts that the last computed result matches the expected value.
     *
     * @param expected expected numeric result
     */
    @Step(value = "custom.java.assert.result", args = "expected:int")
    public void assertResult(
            Integer expected
    ) {
        Assert.assertEquals(expected.intValue(), result);
    }

    /**
     * Schedules a debug action to be executed during teardown.
     *
     * @param action action label written to logs in post-execution phase
     */
    @Step(value = "custom.java.schedule.post.execution", args = "action:text")
    public void schedulePostExecutionAction(
            String action
    ) {
        postExecutionRunners.add(() -> LOGGER.debug("[custom-java-step][post] {}", action));
    }

    /**
     * Runs and clears deferred post-execution actions.
     */
    @TearDown(order = 1)
    public void runPostExecutionActions() {
        postExecutionRunners.forEach(Runnable::run);
        postExecutionRunners.clear();
    }

}
