package es.iti.wakamiti.xray.internal;

import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.azure.AzureSynchronizer;
import es.iti.wakamiti.azure.api.model.TestSuite;
import es.iti.wakamiti.xray.XRaySynchronizer;

import java.util.stream.Stream;


public class ScenarioMapper extends Mapper {

    public ScenarioMapper(String suiteBase) {
        super(suiteBase);
    }

    @Override
    protected Stream<Pair<PlanNodeSnapshot, TestSuite>> suiteMap(PlanNodeSnapshot target) {
        return super.suiteMap(target)
                .flatMap(p ->
                        p.key().flatten(node -> gherkinType(node).equals(type()))
                                .map(node -> new Pair<>(node, new TestSuite().name(p.key().getName()).parent(p.value())))
                );
    }

    @Override
    public String type() {
        return XRaySynchronizer.GHERKIN_TYPE_SCENARIO;
    }

}