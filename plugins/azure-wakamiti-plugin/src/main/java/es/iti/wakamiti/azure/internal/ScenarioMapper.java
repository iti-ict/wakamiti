/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.internal;


import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.azure.AzureSynchronizer;
import es.iti.wakamiti.azure.api.model.TestSuite;

import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class ScenarioMapper extends Mapper {

    public ScenarioMapper(String suiteBase) {
        super(suiteBase);
    }

    @Override
    protected Stream<Pair<PlanNodeSnapshot, TestSuite>> suiteMap(PlanNodeSnapshot target) {
        return super.suiteMap(target)
                .flatMap(p ->
                        p.key().flatten(node -> gherkinType(node).equals(type()))
                                .map(node -> new Pair<>(node, target.getProperties().containsKey(AZURE_SUITE) ?
                                        p.value() : new TestSuite().name(p.key().getName()).parent(p.value())))
                );
    }

    @Override
    public String type() {
        return AzureSynchronizer.GHERKIN_TYPE_SCENARIO;
    }

}
