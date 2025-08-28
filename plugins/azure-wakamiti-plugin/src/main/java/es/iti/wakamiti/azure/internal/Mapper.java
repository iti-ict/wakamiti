/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.internal;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.MapUtils;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.azure.api.model.*;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static es.iti.wakamiti.api.util.StringUtils.format;
import static es.iti.wakamiti.azure.AzureSynchronizer.GHERKIN_TYPE_FEATURE;
import static es.iti.wakamiti.azure.AzureSynchronizer.GHERKIN_TYPE_SCENARIO;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.text.StringEscapeUtils.escapeEcmaScript;


/**
 * Abstract base class for mapping entities from plan
 * nodes to Azure test artifacts, such as test cases,
 * suites, and results.
 */
public abstract class Mapper {

    protected static final String AZURE_SUITE = "azureSuite";
    private final String suiteBase;

    /**
     * Constructs a Mapper with a specified base directory for test suites.
     *
     * @param suiteBase the base directory for mapping test suites.
     */
    protected Mapper(final String suiteBase) {
        this.suiteBase = suiteBase;
    }

    /**
     * Returns an instantiator for a specific type of mapper
     * based on the provided Gherkin type.
     *
     * @param type the Gherkin type (e.g., feature or scenario).
     * @return an instantiator that creates a mapper for the specified type.
     */
    public static Instantiator ofType(String type) {
        return MapUtils.<String, Instantiator>map(
                GHERKIN_TYPE_FEATURE, FeatureMapper::new,
                GHERKIN_TYPE_SCENARIO, ScenarioMapper::new
        ).get(type);
    }

    /**
     * Maps a target plan node to a stream of suite-test pairs.
     *
     * @param target the target plan node snapshot to map.
     * @return a stream of pairs, where each pair contains a plan
     * node and its corresponding test suite.
     */
    protected Stream<Pair<PlanNodeSnapshot, TestSuite>> suiteMap(PlanNodeSnapshot target) {
        Path suitePath = target.getProperties().entrySet().stream()
                .filter(k -> k.getKey().equals(AZURE_SUITE))
                .map(Map.Entry::getValue)
                .map(v -> v.replaceAll(escapeEcmaScript("[\\][/\\]"), TestSuite.SLASH_CODE))
                .map(v -> v.replaceAll(escapeEcmaScript("[/\\]"), "/"))
                .map(Path::of)
                .findFirst().orElseGet(() -> {
                    Path path = Path.of(target.getSource()
                            .replaceAll("(/[^./]+?\\.[^./]+?)?\\[.+?]$", ""));
                    if (!isBlank(suiteBase)) {
                        if (!path.startsWith(suiteBase)) {
                            throw new WakamitiException("Invalid suiteBase: {}", suiteBase);
                        }
                        path = Path.of(suiteBase).relativize(path);
                    }
                    return path;
                });

        TestSuite suite = Stream.of(suitePath.toString().split(escapeEcmaScript(File.separator)))
                .map(dir -> new TestSuite().name(dir.replace(TestSuite.SLASH_CODE, "/"))
                        .suiteType(TestSuite.Type.STATIC_TEST_SUITE))
                .reduce((a, b) -> b.parent(a.hasChildren(true)))
                .orElseThrow();

        return Stream.of(new Pair<>(target, suite));
    }

    /**
     * Maps a single plan node snapshot to a test case.
     *
     * @param idx      the order of the test case within its suite.
     * @param suite  the test suite to which the test case belongs.
     * @param target the plan node snapshot to map.
     * @return a test case representing the mapped plan node snapshot.
     */
    protected TestCase caseMap(int idx, TestSuite suite, PlanNodeSnapshot target) {
        String id = Optional.of(target.getId()).filter(i -> !i.startsWith("#"))
                .orElseThrow(() -> new WakamitiException("Target {} needs the idTag", gherkinType(target)));
        return new TestCase()
                .name(format("[{}] {}", id, target.getName()))
//                .description(join(target.getDescription(), System.lineSeparator()))
                .tag("wakamiti")
                .order(idx)
                .suite(suite.hasChildren(true))
                .metadata(target);
    }

    /**
     * Maps a plan node snapshot to a test result.
     *
     * @param target the plan node snapshot to map.
     * @return a test result representing the mapped plan node snapshot.
     */
    protected TestResult resultMap(PlanNodeSnapshot target) {
        return new TestResult()
                .outcome(TestResult.Type.valueOf(target.getResult()))
                .startedDate(target.getStartInstant())
                .completedDate(target.getFinishInstant())
                .errorMessage(target.getErrorMessage())
            ;
    }

    /**
     * Maps all test cases from a given plan node snapshot.
     *
     * @param plan the plan node snapshot to process.
     * @return a stream of mapped test cases.
     */
    public Stream<TestCase> mapTests(PlanNodeSnapshot plan) {
        return getSuites(plan)
                .entrySet().stream().flatMap(e ->
                    IntStream.range(0, e.getValue().size()).mapToObj(i -> caseMap(i, e.getKey(), e.getValue().get(i)))
                );

    }

    /**
     * Maps all test results from a given plan node snapshot.
     *
     * @param plan the plan node snapshot to process.
     * @return a stream of mapped test results.
     */
    public Stream<TestResult> mapResults(PlanNodeSnapshot plan) {
        return getSuites(plan)
                .entrySet().stream().flatMap(e ->
                        IntStream.range(0, e.getValue().size()).mapToObj(i ->
                                resultMap(e.getValue().get(i))
                                        .testCase(caseMap(i, e.getKey(), e.getValue().get(i)))
                        )
                );
    }

    /**
     * Extracts and groups suites and their associated
     * plan nodes from a plan node snapshot.
     *
     * @param plan the plan node snapshot to process.
     * @return a map where each key is a test suite, and
     * the value is a list of associated plan nodes.
     */
    private Map<TestSuite, List<PlanNodeSnapshot>> getSuites(PlanNodeSnapshot plan) {
        return plan
                .flatten(node -> gherkinType(node).equals(GHERKIN_TYPE_FEATURE))
                .flatMap(this::suiteMap)
                .collect(groupingBy(Pair::value, mapping(Pair::key, toList())));
    }

    /**
     * Returns the type of this mapper as a string.
     *
     * @return the type of the mapper (e.g., "feature" or "scenario").
     */
    public abstract String type();

    /**
     * Retrieves the Gherkin type (e.g., feature, scenario) from a plan node snapshot.
     *
     * @param node the plan node snapshot to inspect.
     * @return the Gherkin type of the node, or an empty string if not defined.
     */
    protected String gherkinType(PlanNodeSnapshot node) {
        return Optional.ofNullable(node.getProperties()).map(p -> p.get("gherkinType")).orElse("");
    }

    /**
     * Functional interface for creating Mapper instances.
     */
    public interface Instantiator {

        /**
         * Creates a new instance of a mapper with the specified suite base.
         *
         * @param suiteBase the base directory for mapping test suites.
         * @return a new Mapper instance.
         */
        Mapper instance(String suiteBase);
    }
}
