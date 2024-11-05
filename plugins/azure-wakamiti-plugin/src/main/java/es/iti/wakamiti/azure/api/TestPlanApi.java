/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api;


import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.TypeRef;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.azure.AzureReporter;
import es.iti.wakamiti.azure.api.model.*;
import es.iti.wakamiti.azure.api.model.query.Query;
import es.iti.wakamiti.azure.api.model.query.WorkItemsQuery;
import es.iti.wakamiti.azure.internal.Util;
import es.iti.wakamiti.azure.internal.WakamitiAzureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static es.iti.wakamiti.api.util.JsonUtils.*;
import static es.iti.wakamiti.api.util.StringUtils.format;
import static es.iti.wakamiti.azure.api.model.query.Field.*;
import static es.iti.wakamiti.azure.api.model.query.criteria.Criteria.field;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.*;


public class TestPlanApi extends BaseApi<TestPlanApi> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureReporter.class);

    private transient Function<String, String> tagExtractor;
    private final String configuration;
    private Settings settings;

    public TestPlanApi(URL baseUrl, Function<String, String> tagExtractor, String configuration) {
        super(baseUrl);
        this.tagExtractor = tagExtractor;
        this.configuration = configuration;
    }

    public Settings settings() {
        if (settings != null) return settings;

        Settings settings = new Settings();

        CompletableFuture<?> future = newRequest()
                .getAsync(finalPathParams.get(ORGANIZATION) + "/_api/_common/GetUserProfile")
                .thenAcceptAsync(response -> response.body().ifPresent(json -> {
                    String tzId = Optional.ofNullable(readStringValue(json, "userPreferences.TimeZoneId"))
                            .orElse(readStringValue(json, "allTimeZones[0].Id"));
                    String tzDisplay = readStringValue(json,
                            format("allTimeZones.find{ it.Id == '{}' }.DisplayName", tzId));
                    ZoneId zid = ZoneId.of(tzDisplay.replaceAll(".*\\(([^()]++)\\).*", "$1"));
                    settings.zoneId(zid);
                }))
                .exceptionally(t -> {
                    if (t != null) {
                        LOGGER.warn("The zone id could not be obtained. Using default: {}", ZoneId.systemDefault(), t);
                        settings.zoneId(ZoneId.systemDefault());
                    }
                    return null;
                });
        settings.configuration(getConfiguration());
        future.join();

        return (this.settings = settings);
    }

    private Integer getConfiguration() {
        Stream<JsonNode> req = newRequest().getAllPages(project() + "/testplan/configurations",
                json -> read(json, "$.value", new TypeRef<>() {}));

        Function<JsonNode, Integer> mapper = json -> read(json, "$.id", Integer.class);
        if (isNotBlank(configuration)) {
            return req.filter(json -> equalsIgnoreCase(read(json, "$.name", String.class), configuration))
                    .findFirst().map(mapper).orElseThrow(() ->
                            new WakamitiAzureException("There is no configuration with name '{}' available. ", configuration));
        } else {
            return req.filter(json -> equalsIgnoreCase(read(json, "$.isDefault", String.class), "true"))
                    .findFirst().map(mapper).orElseThrow(() ->
                            new WakamitiAzureException("There is no default configuration available. "));
        }
    }

    private Optional<TestPlan> getTestPlan(String id) {
        return newRequest()
                .pathParam("planId", id)
                .get(project() + "/testplan/plans/{planId}").body()
                .map(json -> read(json, TestPlan.class));
    }

    private TestPlan createTestPlan(TestPlan plan) {
        String path = project() + "/testplan/plans";
        return newRequest()
                .body(json(plan).toString())
                .post(path)
                .body()
                .map(json -> read(json, TestPlan.class))
                .orElseThrow(() -> new WakamitiException("Something went wrong: empty body"));
    }

    public Optional<TestPlan> searchTestPlan(TestPlan plan) {
        Query query = new WorkItemsQuery()
                .select().where(
                        field(teamProject()).isEqualsTo("@project")
                                .and(field(workItemType()).isInGroup(TestPlan.CATEGORY))
                                .and(field(title()).isEqualsTo(plan.name()))
                                .and(field(areaPath()).isEqualsTo(plan.area()))
                                .and(field(iterationPath()).isEqualsTo(plan.iteration()))
                );
        List<String> ids = doQuery(query)
                .map(a -> a.findValuesAsText("id"))
                .orElseGet(ArrayList::new);
        if (ids.size() > 1) {
            throw new WakamitiAzureException("Too many test plans with same 'name', 'area' and 'iteration': {}. ", ids);
        }
        return ids.stream().findFirst().flatMap(this::getTestPlan);
    }

    /**
     * Get or create a test plan.
     *
     * @param plan
     * @param createItemsIfAbsent
     * @return
     */
    public TestPlan getTestPlan(TestPlan plan, boolean createItemsIfAbsent) {
        return searchTestPlan(plan)
                .orElseGet(() -> {
                    if (createItemsIfAbsent) {
                        return createTestPlan(plan);
                    } else {
                        throw new WakamitiAzureException(
                                "Test Plan with name '{}', area '{}' and iteration '{}' does not exist in Azure. ",
                                plan.name(), plan.area(), plan.iteration());
                    }
                });
    }


    public Stream<TestSuite> searchTestSuites(TestPlan plan) {
        return newRequest()
                .pathParam("planId", plan.id())
                .queryParam("asTreeView", true)
                .getAllPages(project() + "/testplan/Plans/{planId}/suites",
                        json -> {
                            List<TestSuiteTree> trees = read(json, "$.value", new TypeRef<>() {});
                            return Util.readTree(trees);
                        });
    }

    /**
     * Performs an asynchronous post to create each test suite. Once created, it is mapped to {@link TestSuite}
     * object and waits for them all to complete.
     *
     * @param plan   The current test plan
     * @param suites The test suites to be created
     * @return The test suites created
     */
    public List<TestSuite> createTestSuites(TestPlan plan, List<TestSuite> suites) {
        List<CompletableFuture<HttpResponse<Optional<JsonNode>>>> futures = suites.stream()
                .map(suite -> newRequest()
                        .pathParam("planId", plan.id())
                        .body(json(suite.suiteType(TestSuite.Type.staticTestSuite)).toString())
                        .postAsync(project() + "/testplan/Plans/{planId}/suites"))
                .collect(Collectors.toList());
        List<TestSuite> result = new LinkedList<>();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> futures.forEach(future -> {
                    try {
                        future.get().body().map(json -> read(json, TestSuite.class))
                                .ifPresentOrElse(result::add, () -> {
                                    throw new NoSuchElementException("Something went wrong: empty body");
                                });
                    } catch (Exception e) {
                        throw new WakamitiException("Error creating Test Suite", e);
                    }
                })).join();
        return Util.filterHasChildren(result);
    }

    /**
     * Get or create test suites.
     *
     * @param plan
     * @param suites
     * @param createItemsIfAbsent
     * @return
     */
    public List<TestSuite> getTestSuites(TestPlan plan, List<TestSuite> suites, boolean createItemsIfAbsent) {
        List<TestSuite> remoteSuites = searchTestSuites(plan)
                .flatMap(Util::flatten).distinct().collect(Collectors.toList());
        List<TestSuite> newSuites = suites.stream()
                .filter(s -> !remoteSuites.contains(s))
                .peek(s -> remoteSuites.stream().filter(p -> p.equals(s.parent())).findFirst().ifPresent(s::parent))
                .collect(Collectors.toList());
        if (createItemsIfAbsent && !newSuites.isEmpty()) {
            createTestSuites(plan, newSuites).stream()
                    .peek(s -> LOGGER.trace("Remote suite #{} created", s.id()))
                    .forEach(remoteSuites::add);
        }
        return remoteSuites;
    }

    public void removeOrphans(List<TestSuite> suites) {

    }


    public Stream<TestCase> searchTestCases(TestPlan plan) {
        return searchTestSuites(plan)
                .flatMap(Util::flatten).distinct()
                .flatMap(suite -> getTestCases(plan, suite));
    }

    public Stream<TestCase> getTestCases(TestPlan plan, TestSuite suite) {
        return newRequest()
                .pathParam("planId", plan.id())
                .pathParam("suiteId", suite.id())
                .queryParam("excludeFlags", 2)
                .queryParam("expand", true)
                .queryParam("witFields", join(List.of(ID, TITLE, TAGS), ","))
                .getAllPages(project() + "/testplan/Plans/{planId}/Suites/{suiteId}/TestCase",
                        new TypeRef<List<JsonNode>>() {})
                .map(json -> {
                    WorkItem item = read(json, "$.workItem", new TypeRef<>() {});
                    return new TestCase().id(item.id()).name(item.name()).suite(suite)
                            .tag(tagExtractor.apply(item.workItemFields().get(TAGS)))
                            .order(Optional.ofNullable(read(json, "$.order", Integer.class)).orElse(0))
                            .pointAssignments(read(json, "$.pointAssignments"));
                });
    }

    public List<TestCase> createTestCases(TestPlan plan, List<TestCase> testCases) {
        BiFunction<String, String, WorkItemOp> newWorkItem = (field, value) -> new WorkItemOp()
                .op(WorkItemOp.Operation.replace).path(format("/fields/{}", field)).value(value);


//        List<CompletableFuture<HttpResponse<Optional<JsonNode>>>> futures = testCases.stream()
//                .map(suite -> newRequest()
//                        .pathParam("planId", plan.id())
//                        .body(json(suite).toString())
//                        .postAsync(project() + "/testplan/Plans/{planId}/suites"))
//                .collect(Collectors.toList());

        List<TestCase> result = new LinkedList<>();

        return result;
    }

    public List<TestCase> updateTestCases(TestPlan plan, List<Pair<TestCase, TestCase>> testCases) {
        BiFunction<String, String, WorkItemOp> newWorkItem = (field, value) -> new WorkItemOp()
                .op(WorkItemOp.Operation.replace).path(format("/fields/{}", field)).value(value);

        List<TestCase> remove = new LinkedList<>();
        List<TestCase> add = new LinkedList<>();
        List<CompletableFuture<HttpResponse<Optional<JsonNode>>>> futures = testCases.stream().map(p -> {
            List<WorkItemOp> ops = new ArrayList<>();
            TestCase oldT = p.key();
            TestCase newT = p.value();
            if (!oldT.name().equals(newT.name())) {
                ops.add(newWorkItem.apply(TITLE, newT.name()));
            }
            if (!oldT.description().equals(newT.description())) {
                ops.add(newWorkItem.apply(DESCRIPTION, newT.description()));
            }
            if (!oldT.suite().equals(newT.suite())) {
                remove.add(oldT);
                add.add(newT);
            }
            return newRequest()
                    .pathParam("id", oldT.id())
                    .body(json(ops).toString())
                    .patchAsync(project() + "/wit/workitems/{id}");
        }).collect(Collectors.toList());

        // remove suite relation
        remove.stream().collect(groupingBy(TestCase::suite, mapping(TestCase::id, toList())))
                .entrySet()
                .stream()
                .map(e -> newRequest()
                        .pathParam("planId", plan.id())
                        .pathParam("suiteId", e.getKey().id())
                        .pathParam("testCaseIds", join(e.getValue(), ","))
                        .deleteAsync("/testplan/Plans/{planId}/Suites/{suiteId}/TestCase/{testCaseIds}"))
                .forEach(futures::add);

        // add suite relation
        add.stream().collect(groupingBy(TestCase::suite, mapping(TestCase::id, toList())))
                .entrySet()
                .stream()
                .map(e -> newRequest()
                        .pathParam("planId", plan.id())
                        .pathParam("suiteId", e.getKey().id())
                        .pathParam("testCaseIds", join(e.getValue(), ","))
                        .postAsync("/testplan/Plans/{planId}/Suites/{suiteId}/TestCase/{testCaseIds}"))
                .forEach(futures::add);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> futures.forEach(future -> {
                    try {
                        future.get();
                    } catch (Exception e) {
                        throw new WakamitiException("Error updating Test Case", e);
                    }
                })).join();

        return testCases.stream().map(p -> p.key().merge(p.value())).collect(toList());
    }

    @Override
    public TestPlanApi copy() {
        TestPlanApi clone = super.copy();
        clone.tagExtractor = tagExtractor;
        return clone;
    }
}
