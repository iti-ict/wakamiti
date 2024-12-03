package es.iti.wakamiti.xray.internal;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.MapUtils;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.xray.model.JiraIssue;
import es.iti.wakamiti.xray.model.TestCase;
import es.iti.wakamiti.xray.model.TestSet;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static es.iti.wakamiti.xray.XRaySynchronizer.GHERKIN_TYPE_FEATURE;
import static es.iti.wakamiti.xray.XRaySynchronizer.GHERKIN_TYPE_SCENARIO;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.join;


public abstract class Mapper {

    private final String suiteBase;

    protected Mapper(final String suiteBase) {
        this.suiteBase = suiteBase;
    }

    public static Instancer ofType(String type) {
        return MapUtils.<String, Instancer>map(
                GHERKIN_TYPE_FEATURE, FeatureMapper::new,
                GHERKIN_TYPE_SCENARIO, ScenarioMapper::new
        ).get(type);
    }

    protected Stream<Pair<PlanNodeSnapshot, TestSet>> suiteMap(PlanNodeSnapshot target) {
        Path suitePath = Path.of(target.getSource()
                .replaceAll("(/[^./]+?\\.[^./]+?)?\\[.+?]$", ""));
        if (!isBlank(suiteBase)) {
            suitePath = Path.of(suiteBase).relativize(suitePath);
        }

        TestSet suite = new TestSet().issue(new JiraIssue().summary(suitePath.toString()));

        return Stream.of(new Pair<>(target, suite));
    }

    protected TestCase caseMap(TestSet suite, PlanNodeSnapshot target) {
        return new TestCase()
                .issue(new JiraIssue()
                        .summary(target.getName())
                        .description(join(target.getDescription(), System.lineSeparator()))
                        .labels(Collections.singletonList(
                                Optional.of(target.getId()).filter(id -> !id.startsWith("#"))
                                        .orElseThrow(() -> new WakamitiException("Target {} needs the idTag", gherkinType(target)))
                        ))
                )
                .testSetList("".equals(suite.getJira().getSummary()) ? Collections.emptyList() : Collections.singletonList(suite));
    }

    public Stream<TestCase> map(PlanNodeSnapshot plan) {
        return plan
                .flatten(node -> gherkinType(node).equals(GHERKIN_TYPE_FEATURE))
                .flatMap(this::suiteMap)
                .collect(groupingBy(Pair::value, mapping(Pair::key, toList())))
                .entrySet().stream().flatMap(e ->
                        IntStream.range(0, e.getValue().size()).mapToObj(i -> caseMap(e.getKey(), e.getValue().get(i)))
                );

    }

    public abstract String type();

    protected String gherkinType(PlanNodeSnapshot node) {
        return Optional.ofNullable(node.getProperties()).map(p -> p.get("gherkinType")).orElse("");
    }

    public interface Instancer {
        Mapper instance(String suiteBase);
    }
}
