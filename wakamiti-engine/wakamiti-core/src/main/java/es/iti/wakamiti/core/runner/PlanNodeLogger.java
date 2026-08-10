/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.runner;


import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.slf4j.Logger;

import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.model.ExecutionState;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.Argument;


/**
 * Utility class for logging information related to
 * the execution of a test plan.
 */
public class PlanNodeLogger {

    private static final int HEADING_PADDING = 4;
    private static final float MILLIS_PER_SECOND = 1000f;
    private final boolean showStepSource;
    private final boolean showElapsedTime;
    private final List<String> hiddenPatterns;
    private final Logger logger;

    private final long totalNumberTestCases;
    private long currentTestCaseNumber;

    /**
     * Creates an execution logger and derives presentation options from the
     * effective configuration.
     *
     * @param logger        logging backend
     * @param configuration source of step-source, elapsed-time flags, and hidden
     *                      properties
     * @param plan          root plan used to compute test-case progress totals
     */
    public PlanNodeLogger(
            Logger logger,
            Configuration configuration,
            PlanNode plan
    ) {
        this.logger = logger;
        this.showStepSource = configuration
                .get(WakamitiConfiguration.LOGS_SHOW_STEP_SOURCE, Boolean.class)
                .orElse(true);
        this.showElapsedTime = configuration
                .get(WakamitiConfiguration.LOGS_SHOW_ELAPSED_TIME, Boolean.class)
                .orElse(true);
        this.hiddenPatterns = configuration
                .getList(WakamitiConfiguration.PROPERTIES_HIDDEN, String.class)
                .stream()
                .map(p -> "\\$\\{" + p.trim() + "(\\.[\\w\\d-]+)*\\}")
                .toList();
        this.totalNumberTestCases = plan.numDescendants(NodeType.TEST_CASE);
    }

    private static Object emptyIfNull(
            Object value
    ) {
        return value == null ? "" : value;
    }

    /**
     * Logs the header information for the entire test plan.
     *
     * @param plan The root node of the test plan.
     */
    public void logTestPlanHeader(
            PlanNode plan
    ) {
        if (logger.isInfoEnabled()) {
            int numTestCases = plan.numDescendants(NodeType.TEST_CASE);
            logger.info("{!important} Running Test Plan with {} Test Cases...", numTestCases);
        }
    }

    /**
     * Logs the overall result of the test plan.
     *
     * @param plan The root node of the test plan.
     */
    public void logTestPlanResult(
            PlanNode plan
    ) {
        if (logger.isInfoEnabled()) {
            Result result = plan.result().orElse(Result.ERROR);
            int numTestCases = plan.numDescendants(NodeType.TEST_CASE);
            int numTestCasesPassed = plan.numDescendants(NodeType.TEST_CASE, Result.PASSED);
            String resultStyle = "stepResult." + plan.result().orElse(null);
            logger.info("{!" + resultStyle + "}=========================");
            if (result.isPassed()) {
                logger.info("{!" + resultStyle + "}Test Plan {}", result);
            } else {
                logger.info(
                        "{!" + resultStyle + "}Test Plan {}  ({} of {} test cases not passed)",
                        result,
                        numTestCases - numTestCasesPassed,
                        numTestCases
                );
            }
            logger.info("{!" + resultStyle + "}=========================");
        }
    }

    /**
     * Logs the header information for a specific test case.
     *
     * @param node The test case node.
     */
    public void logTestCaseHeader(
            PlanNode node
    ) {
        if (node.nodeType() != NodeType.TEST_CASE) {
            return;
        }
        currentTestCaseNumber++;
        if (logger.isInfoEnabled()) {
            StringJoiner name = new StringJoiner(" : ");
            if (node.keyword() != null) {
                name.add(node.keyword());
            }
            name.add(resolveNodeName(node));
            logger.info("{highlight}", "-".repeat(name.length() + HEADING_PADDING));
            logger.info(
                    "{highlight} (Test Case {}/{})",
                    "| " + name + " |",
                    currentTestCaseNumber,
                    totalNumberTestCases
            );
            logger.info("{highlight}", "-".repeat(name.length() + HEADING_PADDING));
        }
    }

    /**
     * Logs the result of a specific step.
     *
     * @param step The step node.
     */
    public void logStepResult(
            PlanNode step
    ) {
        if (step.nodeType() != NodeType.STEP) {
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info(buildMessage(step), buildMessageArgs(step));
        }
        step.executionState().flatMap(ExecutionState::error)
                .ifPresent(error -> logger.debug("stack trace:", error));
    }

    /**
     * Builds a log message template for a specific step node.
     *
     * @param step The step node for which the message template is built.
     * @return The log message template.
     */
    private String buildMessage(
            PlanNode step
    ) {
        String resultStyle = "stepResult." + step.result().orElse(null);
        StringBuilder message = new StringBuilder();
        message.append("{highlight} {" + resultStyle + "} {highlight} ");
        if (showStepSource) {
            message.append("{source} :");
        }
        message.append(" {keyword} {}");
        if (showElapsedTime) {
            message.append(" {time} ");
        }
        message.append("{" + resultStyle + "}");
        return message.toString();
    }

    /**
     * Builds the arguments for the log message based on the
     * execution state of a specific step node.
     *
     * @param step The step node for which the log message arguments are built.
     * @return An array of objects representing the log message arguments.
     */
    private Object[] buildMessageArgs(
            PlanNode step
    ) {
        ExecutionState<Result> execution = step.executionState().orElse(null);
        if (execution == null) {
            return new Object[0];
        }
        List<Object> args = new ArrayList<>();
        args.add("[");
        args.add(execution.result().orElse(null));
        args.add("]");
        if (showStepSource) {
            args.add(step.source());
        }
        args.add(emptyIfNull(step.keyword()));
        args.add(resolveNodeName(step));
        if (showElapsedTime) {
            String duration = (execution.result().orElse(null) == Result.SKIPPED ? ""
                    : "(" + (execution.duration().map(Duration::toMillis).orElse(0L) / MILLIS_PER_SECOND) + ")");
            args.add(duration);
        }
        args.add(execution.error().map(Throwable::getLocalizedMessage).orElse(""));
        return args.toArray();
    }

    /**
     * Returns the node name with variable placeholders replaced by their
     * resolved values, except for variables listed under
     * {@link WakamitiConfiguration#PROPERTIES_HIDDEN}, which remain masked
     * as {@code ${...}}.
     * <p>
     * This mirrors the logic in {@code Wakamiti.writeOutputFile()} but applies
     * it at log time so that the console output shows actual values rather than
     * raw placeholders (issue #1).
     * </p>
     *
     * @param node the plan node whose name is to be resolved
     * @return the name with visible variable placeholders replaced, or
     *         {@code null} when the node has no name
     */
    private String resolveNodeName(
            PlanNode node
    ) {
        if (node.name() == null) {
            return null;
        }
        Map<String, String> evaluations = node.arguments().stream()
                .map(Argument::evaluations)
                .reduce(new LinkedHashMap<>(), (acc, map) -> {
                    map.forEach(acc::putIfAbsent);
                    return acc;
                });
        String resolved = node.name();
        for (Map.Entry<String, String> entry : evaluations.entrySet()) {
            boolean hidden = hiddenPatterns.stream()
                    .anyMatch(pattern -> entry.getKey().matches(pattern));
            if (!hidden) {
                resolved = resolved.replace(entry.getKey(), entry.getValue());
            }
        }
        return resolved;
    }

}
