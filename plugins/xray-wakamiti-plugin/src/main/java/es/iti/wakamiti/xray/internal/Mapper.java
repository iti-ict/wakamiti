package es.iti.wakamiti.xray.internal;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.MapUtils;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.azure.api.model.TestCase;
import es.iti.wakamiti.azure.api.model.TestSuite;
import es.iti.wakamiti.xray.model.XRayTestCase;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static es.iti.wakamiti.azure.AzureSynchronizer.GHERKIN_TYPE_FEATURE;
import static es.iti.wakamiti.azure.AzureSynchronizer.GHERKIN_TYPE_SCENARIO;
import static es.iti.wakamiti.xray.XRaySynchronizer.GHERKIN_TYPE_FEATURE;
import static es.iti.wakamiti.xray.XRaySynchronizer.GHERKIN_TYPE_SCENARIO;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.text.StringEscapeUtils.escapeEcmaScript;


public abstract class Mapper {

    private static final String AZURE_SUITE = "azureSuite";
    private final String suiteBase;

    public Mapper(final String suiteBase) {
        this.suiteBase = suiteBase;
    }

    public static Instancer ofType(String type) {
        return MapUtils.<String, Instancer>map(
                GHERKIN_TYPE_FEATURE, FeatureMapper::new,
                GHERKIN_TYPE_SCENARIO, ScenarioMapper::new
        ).get(type);
    }

    protected Stream<Pair<PlanNodeSnapshot, TestSuite>> suiteMap(PlanNodeSnapshot target) {
        Path suitePath = target.getProperties().entrySet().stream()
                .filter(k -> k.getKey().equals(AZURE_SUITE))
                .map(Map.Entry::getValue)
                .map(v -> v.split(escapeEcmaScript("[/\\]")))
                .map(s -> Stream.of(s).map(String::trim).collect(Collectors.joining("/")))
                .map(Path::of)
                .findFirst().orElseGet(() -> {
                    Path path = Path.of(target.getSource()
                            .replaceAll("(/[^./]+?\\.[^./]+?)?\\[.+?]$", ""));
                    if (!isBlank(suiteBase)) {
                        path = path.relativize(Path.of(suiteBase));
                    }
                    return path;
                });

        TestSuite suite = Stream.of(suitePath.toString().split(escapeEcmaScript(File.separator)))
                .map(dir -> new TestSuite().name(dir).suiteType(TestSuite.Type.staticTestSuite))
                .reduce((a, b) -> b.parent(a.hasChildren(true)))
                .orElseThrow();

        return Stream.of(new Pair<>(target, suite));
    }

    protected TestCase caseMap(int i, TestSuite suite, PlanNodeSnapshot target) {
        return new TestCase()
                .name(target.getName())
                .description(join(target.getDescription(), System.lineSeparator()))
                .tag(
                        Optional.of(target.getId()).filter(id -> !id.startsWith("#"))
                                .orElseThrow(() -> new WakamitiException("Target {} needs the idTag", gherkinType(target)))
                )
                .order(i)
                .suite(suite.hasChildren(true))
                .metadata(target);
    }

    public Stream<XRayTestCase> map(PlanNodeSnapshot plan) {
        return plan
                .flatten(node -> gherkinType(node).equals(GHERKIN_TYPE_FEATURE))
                .flatMap(this::suiteMap)
                .collect(groupingBy(Pair::value, mapping(Pair::key, toList())))
                .entrySet().stream().flatMap(e ->
                    IntStream.range(0, e.getValue().size()).mapToObj(i -> caseMap(i, e.getKey(), e.getValue().get(i)))
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