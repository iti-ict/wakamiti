/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.runner;


import static es.iti.wakamiti.api.WakamitiConfiguration.STOP_EXECUTION_ON_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.slf4j.Logger;

import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;


public class PlanRunnerErrorHandlingTest {

    @Test
    public void testPlanRunnerContinuesTopLevelExecutionOnErrorByDefault() {
        PlanNode firstNode = new PlanNode(NodeType.AGGREGATOR, List.of());
        PlanNode secondNode = new PlanNode(NodeType.AGGREGATOR, List.of());
        AtomicInteger firstExecutions = new AtomicInteger();
        AtomicInteger secondExecutions = new AtomicInteger();
        PlanRunner runner = new StubPlanRunner(
                new PlanNode(NodeType.AGGREGATOR, List.of(firstNode, secondNode)),
                WakamitiConfiguration.DEFAULTS,
                List.of(
                        fixedRunner(firstNode, Result.ERROR, firstExecutions),
                        fixedRunner(secondNode, Result.PASSED, secondExecutions)
                )
        );

        runner.run();

        assertThat(firstExecutions).hasValue(1);
        assertThat(secondExecutions).hasValue(1);
        assertThat(secondNode.result()).contains(Result.PASSED);
    }

    @Test
    public void testPlanRunnerStopsTopLevelExecutionWhenEnabled() {
        PlanNode firstNode = new PlanNode(NodeType.AGGREGATOR, List.of());
        PlanNode secondNode = new PlanNode(NodeType.AGGREGATOR, List.of());
        AtomicInteger firstExecutions = new AtomicInteger();
        AtomicInteger secondExecutions = new AtomicInteger();
        PlanRunner runner = new StubPlanRunner(
                new PlanNode(NodeType.AGGREGATOR, List.of(firstNode, secondNode)),
                WakamitiConfiguration.DEFAULTS.append(
                    Configuration.factory().fromPairs(STOP_EXECUTION_ON_ERROR, "true")
                ),
                List.of(
                        fixedRunner(firstNode, Result.ERROR, firstExecutions),
                        fixedRunner(secondNode, Result.PASSED, secondExecutions)
                )
        );

        runner.run();

        assertThat(firstExecutions).hasValue(1);
        assertThat(secondExecutions).hasValue(0);
        assertThat(secondNode.result()).contains(Result.SKIPPED);
    }

    @Test
    public void testPlanNodeRunnerContinuesSiblingExecutionOnErrorByDefault() {
        PlanNode firstNode = new PlanNode(NodeType.STEP, List.of());
        PlanNode secondNode = new PlanNode(NodeType.STEP, List.of());
        AtomicInteger firstExecutions = new AtomicInteger();
        AtomicInteger secondExecutions = new AtomicInteger();
        TestablePlanNodeRunner runner = new TestablePlanNodeRunner(
                new PlanNode(NodeType.AGGREGATOR, List.of(firstNode, secondNode)),
                WakamitiConfiguration.DEFAULTS,
                List.of(
                        fixedRunner(firstNode, Result.ERROR, firstExecutions),
                        fixedRunner(secondNode, Result.PASSED, secondExecutions)
                ),
                false
        );

        runner.runNode();

        assertThat(firstExecutions).hasValue(1);
        assertThat(secondExecutions).hasValue(1);
        assertThat(secondNode.result()).contains(Result.PASSED);
    }

    @Test
    public void testPlanNodeRunnerStopsSiblingExecutionWhenEnabled() {
        PlanNode firstNode = new PlanNode(NodeType.STEP, List.of());
        PlanNode secondNode = new PlanNode(NodeType.STEP, List.of());
        AtomicInteger firstExecutions = new AtomicInteger();
        AtomicInteger secondExecutions = new AtomicInteger();
        TestablePlanNodeRunner runner = new TestablePlanNodeRunner(
                new PlanNode(NodeType.AGGREGATOR, List.of(firstNode, secondNode)),
                WakamitiConfiguration.DEFAULTS.append(
                    Configuration.factory().fromPairs(STOP_EXECUTION_ON_ERROR, "true")
                ),
                List.of(
                        fixedRunner(firstNode, Result.ERROR, firstExecutions),
                        fixedRunner(secondNode, Result.PASSED, secondExecutions)
                ),
                false
        );

        runner.runNode();

        assertThat(firstExecutions).hasValue(1);
        assertThat(secondExecutions).hasValue(0);
        assertThat(secondNode.result()).contains(Result.SKIPPED);
    }

    @Test
    public void testPlanNodeRunnerContinuesTestCaseChildrenAfterPreExecutionErrorByDefault() {
        PlanNode step = new PlanNode(NodeType.STEP, List.of());
        AtomicInteger stepExecutions = new AtomicInteger();
        TestablePlanNodeRunner runner = new TestablePlanNodeRunner(
                new PlanNode(NodeType.TEST_CASE, List.of(step)),
                WakamitiConfiguration.DEFAULTS,
                List.of(fixedRunner(step, Result.PASSED, stepExecutions)),
                true
        );

        runner.runNode();

        assertThat(stepExecutions).hasValue(1);
        assertThat(step.result()).contains(Result.PASSED);
        assertThat(runner.getNode().result()).contains(Result.ERROR);
    }

    @Test
    public void testPlanNodeRunnerStopsTestCaseChildrenAfterPreExecutionErrorWhenEnabled() {
        PlanNode step = new PlanNode(NodeType.STEP, List.of());
        AtomicInteger stepExecutions = new AtomicInteger();
        TestablePlanNodeRunner runner = new TestablePlanNodeRunner(
                new PlanNode(NodeType.TEST_CASE, List.of(step)),
                WakamitiConfiguration.DEFAULTS.append(
                    Configuration.factory().fromPairs(STOP_EXECUTION_ON_ERROR, "true")
                ),
                List.of(fixedRunner(step, Result.PASSED, stepExecutions)),
                true
        );

        runner.runNode();

        assertThat(stepExecutions).hasValue(0);
        assertThat(step.result()).contains(Result.SKIPPED);
        assertThat(runner.getNode().result()).contains(Result.ERROR);
    }

    private static PlanNodeRunner fixedRunner(
            PlanNode node,
            Result result,
            AtomicInteger executions
    ) {
        return new PlanNodeRunner(
                node,
                Configuration.factory().empty(),
                mock(BackendFactory.class),
                new PlanNodeLogger(mock(Logger.class), Configuration.factory().empty(), node)
        ) {
            @Override
            protected Result runNode() {
                executions.incrementAndGet();
                Instant instant = Instant.now();
                getNode().prepareExecution().markStarted(instant);
                getNode().prepareExecution().markFinished(instant, result);
                return result;
            }
        };
    }

    private static final class StubPlanRunner extends PlanRunner {

        private final List<PlanNodeRunner> runners;

        private StubPlanRunner(
                PlanNode plan,
                Configuration configuration,
                List<PlanNodeRunner> runners
        ) {
            super(plan, configuration);
            this.runners = runners;
        }

        @Override
        protected List<PlanNodeRunner> buildRunners(
                boolean dryRun
        ) {
            return runners;
        }

    }

    private static final class TestablePlanNodeRunner extends PlanNodeRunner {

        private final List<PlanNodeRunner> runners;
        private final boolean failPreExecution;

        private TestablePlanNodeRunner(
                PlanNode node,
                Configuration configuration,
                List<PlanNodeRunner> runners,
                boolean failPreExecution
        ) {
            super(
                    node,
                    configuration,
                    mock(BackendFactory.class),
                    new PlanNodeLogger(mock(Logger.class), configuration, node)
            );
            this.runners = runners;
            this.failPreExecution = failPreExecution;
        }

        @Override
        protected List<PlanNodeRunner> createChildren() {
            return runners;
        }

        @Override
        protected void testCasePreExecution(
                PlanNode node
        ) {
            if (failPreExecution) {
                throw new WakamitiException("forced pre-execution error");
            }
        }

        @Override
        protected void testCasePostExecution(
                PlanNode node
        ) {
            // No-op for tests.
        }

    }

}
