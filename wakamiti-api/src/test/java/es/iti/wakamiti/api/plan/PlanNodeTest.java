/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.plan;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;

import org.junit.Test;


public class PlanNodeTest {

    @Test
    public void testLifecycleHooksAffectAggregatedResultButNotCounts() {
        PlanNodeBuilder featureBuilder = new PlanNodeBuilder(NodeType.AGGREGATOR)
                .addProperty("gherkinType", "feature");
        PlanNodeBuilder lifecycleBuilder = new PlanNodeBuilder(NodeType.LIFECYCLE_HOOK)
                .setName("before feature hook")
                .addProperty("gherkinType", "before");
        PlanNodeBuilder functionalBuilder = new PlanNodeBuilder(NodeType.TEST_CASE)
                .setId("ID-Functional")
                .setName("functional scenario")
                .addProperty("gherkinType", "scenario");

        featureBuilder.addChild(lifecycleBuilder);
        featureBuilder.addChild(functionalBuilder);

        PlanNode feature = featureBuilder.build();
        PlanNode lifecycleScenario = feature.children().toList().get(0);
        PlanNode functionalScenario = feature.children().toList().get(1);

        Instant now = Instant.now();
        lifecycleScenario.prepareExecution().markStarted(now);
        lifecycleScenario.prepareExecution().markFinished(now, Result.ERROR);
        functionalScenario.prepareExecution().markStarted(now);
        functionalScenario.prepareExecution().markFinished(now, Result.PASSED);

        assertThat(feature.result()).contains(Result.ERROR);

        PlanNodeSnapshot snapshot = new PlanNodeSnapshot(feature);
        assertThat(snapshot.getTestCaseResults()).isEqualTo(Map.of(Result.PASSED, 1L));
        assertThat(snapshot.getChildrenResults()).isEqualTo(Map.of(Result.PASSED, 1L));
    }

    @Test
    public void testExplicitResultTakesPrecedenceOverChildrenAndEmptyAggregatorHasNoResult() {
        PlanNodeBuilder emptyBuilder = new PlanNodeBuilder(NodeType.AGGREGATOR);
        assertThat(emptyBuilder.build().result()).isEmpty();

        PlanNodeBuilder parentBuilder = new PlanNodeBuilder(NodeType.AGGREGATOR);
        parentBuilder.addChild(new PlanNodeBuilder(NodeType.TEST_CASE)
                .setName("passed child"));
        PlanNode parent = parentBuilder.build();
        parent.prepareExecution().markFinished(Instant.now(), Result.SKIPPED);

        assertThat(parent.result()).contains(Result.SKIPPED);
    }

    @Test
    public void testSnapshotAndNodeInstantsUseSixFractionalDigits() {
        PlanNode node = new PlanNodeBuilder(NodeType.TEST_CASE)
                .setId("ID-Timestamp")
                .setName("timestamp test")
                .build();
        Instant instant = Instant.parse("2026-08-31T15:20:07.723566700Z");
        node.prepareExecution().markStarted(instant);
        node.prepareExecution().markFinished(instant, Result.PASSED);

        PlanNodeSnapshot snapshot = new PlanNodeSnapshot(
                node,
                "2026-08-31T15:20:07.723Z"
        );

        assertThat(snapshot.getSnapshotInstant())
                .isEqualTo("2026-08-31T15:20:07.723000Z");
        assertThat(snapshot.getStartInstant())
                .isEqualTo("2026-08-31T15:20:07.723566Z");
        assertThat(snapshot.getFinishInstant())
                .isEqualTo("2026-08-31T15:20:07.723566Z");
    }

}
