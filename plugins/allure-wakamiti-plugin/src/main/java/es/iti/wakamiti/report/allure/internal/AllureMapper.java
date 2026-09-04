/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.allure.internal;


import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import io.qameta.allure.model.FixtureResult;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Stage;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;


/**
 * Converts Wakamiti snapshots to the typed Allure result model.
 */
public class AllureMapper {

    private static final String WAKAMITI = "wakamiti";

    /**
     * Maps the complete snapshot tree to Allure results and containers.
     *
     * @param root snapshot root
     * @return mapped Allure objects
     */
    public List<WithUuid> map(
            PlanNodeSnapshot root
    ) {
        List<WithUuid> mapped = new ArrayList<>();
        mapNode(root, null, mapped);
        return mapped;
    }

    private void mapNode(
            PlanNodeSnapshot node,
            PlanNodeSnapshot feature,
            List<WithUuid> mapped
    ) {
        if (node == null) {
            return;
        }
        PlanNodeSnapshot activeFeature = isFeature(node) ? node : feature;
        if (node.getNodeType() == NodeType.TEST_CASE) {
            mapped.add(mapTestResult(node, activeFeature));
            return;
        }
        if (isFeature(node)) {
            TestResultContainer container = new TestResultContainer();
            container.setUuid(uuid("container", node.getSource(), node.getName()));
            container.setName(Optional.ofNullable(node.getName()).orElse(displayName(node)));
            container.setDescription(description(node));
            container.setStart(toEpochMillis(node.getStartInstant()));
            container.setStop(toEpochMillis(node.getFinishInstant()));
            List<String> children = new ArrayList<>();
            if (node.getChildren() != null) {
                for (PlanNodeSnapshot child : node.getChildren()) {
                    if (child.getNodeType() == NodeType.LIFECYCLE_HOOK) {
                        if (isExecutedFixture(child)) {
                            addFixture(container, child);
                        }
                        continue;
                    }
                    int before = mapped.size();
                    mapNode(child, activeFeature, mapped);
                    for (int i = before; i < mapped.size(); i++) {
                        if (mapped.get(i) instanceof TestResult result) {
                            children.add(result.getUuid());
                        }
                    }
                }
            }
            container.setChildren(children);
            if (!children.isEmpty() || !container.getBefores().isEmpty() || !container.getAfters().isEmpty()) {
                mapped.add(container);
            }
            return;
        }
        if (node.getChildren() != null) {
            node.getChildren().forEach(child -> mapNode(child, activeFeature, mapped));
        }
    }

    private void addFixture(
            TestResultContainer container,
            PlanNodeSnapshot hook
    ) {
        String type = hook.getProperties() == null ? null : hook.getProperties().get("gherkinType");
        FixtureResult fixture = mapFixture(hook);
        if ("before".equals(type)) {
            container.getBefores().add(fixture);
        } else if ("after".equals(type)) {
            container.getAfters().add(fixture);
        }
    }

    private FixtureResult mapFixture(
            PlanNodeSnapshot node
    ) {
        return new FixtureResult()
                .setName(node.getName())
                .setDescription(description(node))
                .setStatus(status(node.getResult()))
                .setStatusDetails(statusDetails(node))
                .setStage(Stage.FINISHED)
                .setStart(toEpochMillis(node.getStartInstant()))
                .setStop(toEpochMillis(node.getFinishInstant()))
                .setSteps(mapSteps(node.getChildren()))
                .setParameters(List.of());
    }

    private TestResult mapTestResult(
            PlanNodeSnapshot node,
            PlanNodeSnapshot feature
    ) {
        String fullName = node.getDisplayName();
        return (TestResult) new TestResult()
                .setUuid(uuid("result", node.getExecutionID(), fullName))
                .setHistoryId(hash(fullName))
                .setTestCaseId(node.getId())
                .setFullName(fullName)
                .setName(node.getName())
                .setDescription(description(node))
                .setStatus(status(node.getResult()))
                .setStatusDetails(statusDetails(node))
                .setStage(Stage.FINISHED)
                .setStart(toEpochMillis(node.getStartInstant()))
                .setStop(toEpochMillis(node.getFinishInstant()))
                .setSteps(mapSteps(node.getChildren()))
                .setParameters(testParameters(node))
                .setLabels(labels(feature, node));
    }

    private List<StepResult> mapSteps(
            List<PlanNodeSnapshot> children
    ) {
        List<StepResult> steps = new ArrayList<>();
        if (children != null) {
            for (PlanNodeSnapshot child : children) {
                if (child.getNodeType() != null && child.getNodeType().isAnyOf(
                        NodeType.STEP, NodeType.STEP_AGGREGATOR, NodeType.VIRTUAL_STEP)) {
                    steps.add(mapStep(child));
                }
            }
        }
        return steps;
    }

    private StepResult mapStep(
            PlanNodeSnapshot node
    ) {
        return new StepResult()
                .setName(displayName(node))
                .setDescription(description(node))
                .setStatus(status(node.getResult()))
                .setStatusDetails(statusDetails(node))
                .setStage(Stage.FINISHED)
                .setStart(toEpochMillis(node.getStartInstant()))
                .setStop(toEpochMillis(node.getFinishInstant()))
                .setParameters(List.of())
                .setSteps(mapSteps(node.getChildren()));
    }

    private List<Parameter> testParameters(
            PlanNodeSnapshot node
    ) {
        List<Parameter> parameters = new ArrayList<>();
        addParameter(parameters, "id", node.getId());
        addParameter(parameters, "source", node.getSource());
        return parameters;
    }

    private void addParameter(
            List<Parameter> target,
            String name,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            target.add(new Parameter().setName(name).setValue(value));
        }
    }

    private List<Label> labels(
            PlanNodeSnapshot feature,
            PlanNodeSnapshot node
    ) {
        List<Label> labels = new ArrayList<>();
        addLabel(labels, "framework", WAKAMITI);
        addLabel(labels, "language", Optional.ofNullable(node.getLanguage()).orElse("gherkin"));
        addLabel(labels, "host", hostName());
        addLabel(labels, "thread", Optional.ofNullable(node.getExecutionID()).orElse(WAKAMITI));
        addLabel(labels, "feature", feature == null ? null : feature.getName());
        if (node.getTags() != null) {
            String tags = node.getTags().stream()
                    .filter(Objects::nonNull)
                    .collect(java.util.stream.Collectors.joining(","));
            if (!tags.isBlank()) {
                addLabel(labels, "tags", tags);
            }
        }
        return labels;
    }

    private void addLabel(
            List<Label> target,
            String name,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            target.add(new Label().setName(name).setValue(value));
        }
    }

    private String description(
            PlanNodeSnapshot node
    ) {
        return node.getDescription() == null || node.getDescription().isEmpty()
                ? null : String.join(System.lineSeparator(), node.getDescription());
    }

    private String displayName(
            PlanNodeSnapshot node
    ) {
        if (node.getDisplayName() != null && !node.getDisplayName().isBlank()) {
            return node.getDisplayName();
        }
        String joined = (Optional.ofNullable(node.getKeyword()).orElse("") + " "
                + Optional.ofNullable(node.getName()).orElse("")).trim();
        return joined.isEmpty() ? "Unnamed node" : joined;
    }

    private StatusDetails statusDetails(
            PlanNodeSnapshot node
    ) {
        if (node.getErrorMessage() == null && node.getErrorTrace() == null) {
            return null;
        }
        return new StatusDetails().setMessage(node.getErrorMessage()).setTrace(node.getErrorTrace());
    }

    private Status status(
            Result result
    ) {
        if (result == null) {
            return null;
        }
        return switch (result) {
            case PASSED -> Status.PASSED;
            case FAILED -> Status.FAILED;
            case ERROR, UNDEFINED -> Status.BROKEN;
            case SKIPPED, NOT_IMPLEMENTED -> Status.SKIPPED;
            default -> null;
        };
    }

    private boolean isFeature(
            PlanNodeSnapshot node
    ) {
        return node.getProperties() != null && "feature".equals(node.getProperties().get("gherkinType"));
    }

    private boolean isExecutedFixture(
            PlanNodeSnapshot node
    ) {
        return node.getResult() != null && node.getResult() != Result.SKIPPED;
    }

    private Long toEpochMillis(
            String instant
    ) {
        if (instant == null || instant.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(instant).toEpochMilli();
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(instant).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    }

    private String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            return "localhost";
        }
    }

    private String hash(
            String value
    ) {
        return uuid("history", value).replace("-", "");
    }

    private String uuid(
            String kind,
            String... values
    ) {
        StringBuilder value = new StringBuilder(kind);
        if (values != null) {
            for (String item : values) {
                value.append(':').append(Objects.toString(item, ""));
            }
        }
        return UUID.nameUUIDFromBytes(value.toString().getBytes(StandardCharsets.UTF_8)).toString();
    }

}
