/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.runner;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.slf4j.Logger;

import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeBuilder;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.Argument;


/**
 * Tests for {@link PlanNodeLogger} related to variable placeholder resolution.
 * <p>
 * Reproduces <a href="https://github.com/wakamiti/wakamiti-core/issues/1">issue #1</a>:
 * variable placeholders (e.g. {@code ${credential.name}}) were logged as-is
 * instead of being replaced with their resolved values.
 * They should remain masked only when listed under {@code properties.hidden}.
 * </p>
 */
public class PlanNodeLoggerTest {

    /**
     * Reproduces issue #1: a regular (non-hidden) variable placeholder must
     * appear resolved in the log output after the step executes.
     * <p>
     * This test <strong>currently fails</strong> because
     * {@code PlanNodeLogger.buildMessageArgs()} calls {@code step.name()}
     * without applying the evaluations captured in the step's arguments.
     * </p>
     */
    @Test
    public void testLogStepResultResolvesVariablePlaceholders() {
        // Given: a step whose name contains a variable placeholder
        PlanNode step = new PlanNodeBuilder(NodeType.STEP)
                .setName("the user name is ${credential.name}")
                .build();

        // Simulate the evaluations that RunnableBackend captures during step execution
        Argument resolvedArg = Argument.of("${credential.name}", s -> s);
        resolvedArg.evaluations().put("${credential.name}", "admin");
        step.arguments().add(resolvedArg);

        Instant now = Instant.now();
        step.prepareExecution().markStarted(now);
        step.prepareExecution().markFinished(now, Result.PASSED);

        // When: the logger produces the step-result line
        List<Object> loggedArgs = captureLogArgs(step, Configuration.factory().empty());

        // Then: the logged text must contain the resolved value, not the placeholder
        Optional<String> loggedStepName = loggedArgs.stream()
                .filter(a -> a instanceof String s && s.contains("the user name is"))
                .map(Object::toString)
                .findFirst();

        assertThat(loggedStepName)
                .as("Step name must be logged with ${credential.name} resolved (issue #1)")
                .hasValueSatisfying(name ->
                        assertThat(name)
                                .isEqualTo("the user name is admin")
                                .doesNotContain("${credential.name}")
                );
    }

    /**
     * Ensures that variables listed under {@code properties.hidden} remain
     * masked as {@code ${...}} in the log, even after the issue #1 fix is applied.
     * <p>
     * This test acts as a regression guard: the fix must resolve visible variables
     * but must <em>not</em> expose hidden ones.
     * </p>
     */
    @Test
    public void testLogStepResultKeepsHiddenVariablesMasked() {
        // Given: a step whose name contains a variable marked as hidden
        PlanNode step = new PlanNodeBuilder(NodeType.STEP)
                .setName("the password is ${credential.password}")
                .build();

        Argument resolvedArg = Argument.of("${credential.password}", s -> s);
        resolvedArg.evaluations().put("${credential.password}", "s3cret");
        step.arguments().add(resolvedArg);

        Instant now = Instant.now();
        step.prepareExecution().markStarted(now);
        step.prepareExecution().markFinished(now, Result.PASSED);

        Configuration config = WakamitiConfiguration.DEFAULTS.append(
                Configuration.factory().fromPairs(
                        WakamitiConfiguration.PROPERTIES_HIDDEN, "credential.password"
                )
        );

        // When: the logger produces the step-result line
        List<Object> loggedArgs = captureLogArgs(step, config);

        // Then: the hidden placeholder must NOT be replaced with its actual value
        Optional<String> loggedStepName = loggedArgs.stream()
                .filter(a -> a instanceof String s && s.contains("the password is"))
                .map(Object::toString)
                .findFirst();

        assertThat(loggedStepName)
                .as("Hidden variable ${credential.password} must remain masked in log (issue #1)")
                .hasValueSatisfying(name ->
                        assertThat(name)
                                .isEqualTo("the password is ${credential.password}")
                                .doesNotContain("s3cret")
                );
    }

    /**
     * Runs {@link PlanNodeLogger#logStepResult(PlanNode)} with a mock logger and
     * returns all arguments that were passed to {@code logger.info(String, Object...)}.
     * <p>
     * Handles both Mockito varargs-spreading behaviours: some versions keep an
     * explicit {@code Object[]} as a single element; others spread it.
     * </p>
     */
    private static List<Object> captureLogArgs(
            PlanNode step,
            Configuration config
    ) {
        Logger mockLogger = mock(Logger.class);
        when(mockLogger.isInfoEnabled()).thenReturn(true);

        AtomicReference<Object[]> captured = new AtomicReference<>();
        doAnswer(inv -> {
            captured.set(inv.getArguments());
            return null;
        }).when(mockLogger).info(any(String.class), any(Object[].class));

        new PlanNodeLogger(mockLogger, config, step).logStepResult(step);

        Object[] raw = captured.get();
        assertThat(raw).as("logger.info must have been called").isNotNull();

        // Normalise: raw[0] is the format string; the rest are the message args.
        // If Mockito kept the Object[] as a single element, unwrap it.
        if (raw.length == 2 && raw[1] instanceof Object[] spread) {
            return Arrays.asList(spread);
        }
        return Arrays.asList(Arrays.copyOfRange(raw, 1, raw.length));
    }

}
