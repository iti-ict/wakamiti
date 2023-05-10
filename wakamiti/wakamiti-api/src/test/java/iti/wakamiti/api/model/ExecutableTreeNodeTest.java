/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.api.model;

import iti.wakamiti.api.plan.NodeType;
import iti.wakamiti.api.plan.PlanNode;
import iti.wakamiti.api.plan.Result;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecutableTreeNodeTest {

    private PlanNode plan;

    @Before
    public void setup() {
        List<PlanNode> children = new LinkedList<>();

        for (int i = 0; i < 5; i++) {
            PlanNode child = new PlanNode(NodeType.STEP, new ArrayList<>());
            child.prepareExecution().markStarted(Instant.parse("2018-11-30T18:00:24.00Z").plusSeconds(60 * i));
            child.prepareExecution().markFinished(Instant.parse("2018-11-30T18:00:24.00Z").plusSeconds(60 * (i + 1)), Result.PASSED);
            children.add(child);
        }

        plan = new PlanNode(NodeType.TEST_CASE, children);
    }

    @Test
    public void testDuration() {
        Optional<Duration> result = plan.duration();

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().toMinutes()).isEqualTo(5);
    }

}
