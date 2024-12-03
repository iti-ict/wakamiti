package es.iti.wakamiti.xray.internal;

import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.xray.XRaySynchronizer;
import es.iti.wakamiti.xray.model.JiraIssue;
import es.iti.wakamiti.xray.model.TestSet;

import java.util.stream.Stream;


public class ScenarioMapper extends Mapper {

    public ScenarioMapper(String suiteBase) {
        super(suiteBase);
    }

    @Override
    protected Stream<Pair<PlanNodeSnapshot, TestSet>> suiteMap(PlanNodeSnapshot target) {
        return super.suiteMap(target)
                .flatMap(p ->
                        p.key().flatten(node -> gherkinType(node).equals(type()))
                                .map(node -> new Pair<>(node, new TestSet().issue(new JiraIssue().summary(p.key().getName()))))
                );
    }

    @Override
    public String type() {
        return XRaySynchronizer.GHERKIN_TYPE_SCENARIO;
    }

}