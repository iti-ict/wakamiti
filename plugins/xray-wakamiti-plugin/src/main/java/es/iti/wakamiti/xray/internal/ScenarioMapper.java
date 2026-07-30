/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.internal;


import java.util.stream.Stream;

import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.xray.XRaySynchronizer;
import es.iti.wakamiti.xray.model.JiraIssue;
import es.iti.wakamiti.xray.model.TestSet;


/**
 * Maps Scenario data between its external and internal representations.
 */
public class ScenarioMapper extends Mapper {

    /**
     * Creates a mapper that produces one Xray test for each Wakamiti scenario.
     *
     * @param suiteBase base directory used to relativize generated test-set paths
     */
    public ScenarioMapper(
            String suiteBase
    ) {
        super(suiteBase);
    }

    @Override
    protected Stream<Pair<PlanNodeSnapshot, TestSet>> suiteMap(
            PlanNodeSnapshot target
    ) {
        return super.suiteMap(target)
                .flatMap(p ->
                        p.key().flatten(node -> gherkinType(node).equals(type()))
                                .map(node -> new Pair<>(node, new TestSet().issue(new JiraIssue()
                                        .summary(p.key().getName())
                                        .labels(p.value().getJira().getLabels())))
                                )
                );
    }

    /**
     * Identifies scenarios as the Gherkin level consumed by this mapper.
     *
     * @return the scenario Gherkin type identifier
     */
    @Override
    public String type() {
        return XRaySynchronizer.GHERKIN_TYPE_SCENARIO;
    }

}
