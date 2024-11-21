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
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.api.model.*;
import es.iti.wakamiti.azure.api.model.query.Query;
import es.iti.wakamiti.azure.api.model.query.WorkItemsQuery;
import es.iti.wakamiti.azure.internal.Util;
import es.iti.wakamiti.azure.internal.WakamitiAzureException;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static es.iti.wakamiti.api.util.JsonUtils.*;
import static es.iti.wakamiti.api.util.MapUtils.map;
import static es.iti.wakamiti.api.util.StringUtils.format;
import static es.iti.wakamiti.azure.api.model.TestRun.Status.Completed;
import static es.iti.wakamiti.azure.api.model.query.Field.*;
import static es.iti.wakamiti.azure.api.model.query.criteria.Criteria.field;
import static es.iti.wakamiti.azure.internal.Util.path;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.*;


public class AzureApi extends BaseApi<AzureApi> {

    private static final Logger LOGGER = WakamitiLogger.forClass(AzureApi.class);
    private static final int MAX_LIST = 200;

    private final String configuration;
    private transient Function<String, String> tagExtractor;
    private Settings settings;

    /**
     * Constructs an instance of {@code AzureApi} with the specified base URL,
     * tag extractor function, and configuration.
     *
     * @param baseUrl      The base URL for Azure API requests.
     * @param tagExtractor A function to extract tags from strings.
     * @param configuration The name of the test configuration to use.
     */
    public AzureApi(URL baseUrl, Function<String, String> tagExtractor, String configuration) {
        super(baseUrl);
        this.tagExtractor = tagExtractor;
        this.configuration = configuration;
    }

    /**
     * Retrieves the {@link Settings} object for the Azure API.
     * This includes configuration details such as the time zone and test case category.
     * The settings are initialized lazily and cached for future calls.
     *
     * @return The {@link Settings} object containing Azure API configuration details.
     * @throws WakamitiAzureException If required, settings cannot be retrieved.
     */
    public Settings settings() {
        if (settings != null) return settings;

        Settings settings = new Settings();

        List<CompletableFuture<?>> futures = new ArrayList<>();

        // Retrieves user profile details including the time zone.
        futures.add(newRequest()
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
                })
        );

        // Retrieves the default test case type for the project.
        futures.add(newRequest()
                .pathParam("category", TestCase.CATEGORY)
                .getAsync(project() + "/wit/workitemtypecategories/{category}")
                .thenAcceptAsync(response -> response.body()
                        .map(json -> readStringValue(json, "defaultWorkItemType?.name"))
                        .ifPresentOrElse(settings::testCaseType, () -> {
                            throw new NoSuchElementException("Default test case category");
                        }))
        );

        // Sets the test configuration.
        settings.configuration(getConfiguration());

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (InterruptedException e) {
            throw new WakamitiAzureException("There is no test case category available. ", e);
        } catch (ExecutionException e) {
            throw new WakamitiAzureException("There is no test case category available. ", e.getCause());
        }

        return (this.settings = settings);
    }

    /**
     * Retrieves the ID of the test configuration to use.
     * If a configuration name is specified, its ID is returned.
     * Otherwise, the default configuration ID is returned.
     *
     * @return The test configuration ID.
     * @throws WakamitiAzureException If the specified or default
     *                                configuration cannot be found.
     */
    private String getConfiguration() {
        Stream<JsonNode> req = newRequest().getAllPages(project() + "/testplan/configurations",
                json -> read(json, "$.value", new TypeRef<>() {}));

        Function<JsonNode, String> mapper = json -> read(json, "$.id", String.class);
        if (isNotBlank(configuration)) {
            return req.filter(json -> equalsIgnoreCase(read(json, "$.name", String.class), configuration))
                    .findFirst().map(mapper).orElseThrow(() -> new WakamitiAzureException(
                            "There is no configuration with name '{}' available. ", configuration));
        } else {
            return req.filter(json -> equalsIgnoreCase(read(json, "$.isDefault", String.class), "true"))
                    .findFirst().map(mapper).orElseThrow(() -> new WakamitiAzureException(
                            "There is no default configuration available. "));
        }
    }

    /**
     * Retrieves a test plan by its ID.
     *
     * @param id The ID of the test plan.
     * @return An {@link Optional} containing the {@link TestPlan}, or empty if not found.
     */
    private Optional<TestPlan> getTestPlan(String id) {
        return newRequest()
                .pathParam("planId", id)
                .get(project() + "/testplan/plans/{planId}")
                .body().map(json -> read(json, TestPlan.class));
    }

    /**
     * Creates a new test plan.
     *
     * @param plan The {@link TestPlan} to create.
     * @return The created {@link TestPlan}.
     * @throws NoSuchElementException If the response body is empty.
     */
    private TestPlan createTestPlan(TestPlan plan) {
        return newRequest()
                .body(json(plan).toString())
                .post(project() + "/testplan/plans")
                .body().map(json -> read(json, TestPlan.class))
                .orElseThrow(() -> new NoSuchElementException("Empty body"));
    }

    /**
     * Searches for the ID of a test plan matching the specified attributes.
     *
     * @param plan The {@link TestPlan} to search for.
     * @return An {@link Optional} containing the test plan ID, or empty if not found.
     * @throws WakamitiAzureException If multiple matching test plans are found.
     */
    public Optional<String> searchTestPlanId(TestPlan plan) {
        Query query = new WorkItemsQuery()
                .select().where(
                        field(teamProject()).isEqualsTo("@project")
                                .and(field(workItemType()).isInGroup(TestPlan.CATEGORY))
                                .and(field(title()).isEqualsTo(plan.name()))
                                .and(field(areaPath()).isEqualsTo(plan.area()))
                                .and(field(iterationPath()).isEqualsTo(plan.iteration()))
                );
        List<String> ids = doQuery(query)
                .map(json -> json.findValuesAsText("id"))
                .orElseGet(ArrayList::new);
        if (ids.size() > 1) {
            throw new WakamitiAzureException("Too many test plans with same 'name', 'area' and 'iteration': {}. ", ids);
        }
        return ids.stream().findFirst();
    }

    /**
     * Searches for a test plan matching the specified attributes.
     *
     * @param plan The {@link TestPlan} to search for.
     * @return An {@link Optional} containing the {@link TestPlan}, or empty if not found.
     */
    public Optional<TestPlan> searchTestPlan(TestPlan plan) {
        return searchTestPlanId(plan).flatMap(this::getTestPlan);
    }

    /**
     * Retrieves an existing test plan or creates a new one if it does not exist.
     *
     * @param plan The {@link TestPlan} to retrieve or create.
     * @param createItemsIfAbsent Whether to create the test plan if it is absent.
     * @return The retrieved or newly created {@link TestPlan}.
     * @throws WakamitiAzureException If the test plan does not exist and creation is not allowed.
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

    /**
     * Searches for test suites associated with the specified test plan.
     *
     * @param plan The {@link TestPlan} containing the test suites.
     * @return A {@link Stream} of {@link TestSuite} objects.
     */
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
     * Creates a list of test suites under the specified test plan.
     *
     * @param plan The {@link TestPlan} under which to create the test suites.
     * @param suites The list of {@link TestSuite} objects to create.
     * @return A filtered list of created {@link TestSuite} objects.
     */
    public List<TestSuite> createTestSuites(TestPlan plan, List<TestSuite> suites) {
        Function<TestSuite, TestSuite> newSuite = suite -> new TestSuite().name(suite.name())
                .suiteType(TestSuite.Type.staticTestSuite)
                .parent(isNull(suite.parent()) ? null : new TestSuite().id(suite.parent().id()));
        List<TestSuite> result = suites.stream()
                .map(suite -> newRequest()
                        .pathParam("planId", plan.id())
                        .body(json(newSuite.apply(suite)).toString())
                        .post(project() + "/testplan/Plans/{planId}/suites")
                        .body().map(json -> read(json, TestSuite.class))
                        .orElseThrow(() -> new NoSuchElementException("Empty body")))
                .peek(suite -> suites.stream()
                        .filter(s -> !isNull(suite.parent()) && suite.parent().id().equals(s.id()))
                        .forEach(suite::parent))
                .peek(suite -> suites.stream().filter(s -> suite.equals(s.parent()) && isNull(s.parent().id()))
                        .forEach(s -> s.parent().id(suite.id())))
                .collect(Collectors.toList());
        return Util.filterHasChildren(result);
    }

    /**
     * Retrieves existing test suites or creates new ones if they do not exist.
     *
     * @param plan The {@link TestPlan} containing the test suites.
     * @param suites The list of {@link TestSuite} objects to retrieve or create.
     * @param createItemsIfAbsent Whether to create test suites if they are absent.
     * @return A list of {@link TestSuite} objects.
     */
    public List<TestSuite> getTestSuites(TestPlan plan, List<TestSuite> suites, boolean createItemsIfAbsent) {
        List<TestSuite> remoteSuites = searchTestSuites(plan)
                .flatMap(Util::flatten).distinct().collect(Collectors.toList());
        List<TestSuite> newSuites = suites.stream()
                .flatMap(Util::flatten).distinct()
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

    /**
     * Searches for test cases in a specific test suite under the given test plan.
     *
     * @param plan The {@link TestPlan} containing the test cases.
     * @param suite The {@link TestSuite} containing the test cases.
     * @return A {@link Stream} of {@link TestCase} objects.
     */
    public Stream<TestCase> searchTestCases(TestPlan plan, TestSuite suite) {
        return newRequest()
                .pathParam("planId", plan.id())
                .pathParam("suiteId", suite.id())
                .queryParam("excludeFlags", 2)
                .queryParam("expand", true)
                .queryParam("witFields", join(List.of(TITLE, TAGS), ","))
                .getAllPages(project() + "/testplan/Plans/{planId}/Suites/{suiteId}/TestCase",
                        new TypeRef<List<JsonNode>>() { })
                .map(json -> {
                    WorkItem item = read(json, "$.workItem", new TypeRef<>() {});
                    return new TestCase().id(item.id()).name(item.name()).suite(suite)
                            .tag(tagExtractor.apply(item.workItemFields().get(TAGS)))
                            .order(Optional.ofNullable(read(json, "$.order", Integer.class)).orElse(0))
                            .pointAssignments(read(json, "$.pointAssignments", new TypeRef<>() { }));
                });
    }

    /**
     * Retrieves existing test cases or creates new ones if they do not exist.
     *
     * @param plan The {@link TestPlan} containing the test cases.
     * @param suites The list of {@link TestSuite} objects containing the test cases.
     * @param tests The list of {@link TestCase} objects to retrieve or create.
     * @param createItemsIfAbsent Whether to create test cases if they are absent.
     * @return A list of {@link TestCase} objects.
     */
    public List<TestCase> getTestCases(TestPlan plan, List<TestSuite> suites, List<TestCase> tests,
                                       boolean createItemsIfAbsent) {
        List<TestCase> remoteTests = suites.stream().parallel()
                .flatMap(suite -> searchTestCases(plan, suite))
                .collect(Collectors.toList());
        List<TestCase> newTests = tests.stream().filter(t -> !remoteTests.contains(t))
                .peek(t -> suites.stream().filter(s -> s.equals(t.suite())).findFirst()
                        .ifPresentOrElse(t::suite, () -> {
                            throw new WakamitiAzureException("Suite '{}' not found", t.suite());
                        }))
                .collect(Collectors.toList());
        List<Pair<TestCase, TestCase>> modSuiteTests = remoteTests.stream()
                .filter(t -> tests.stream()
                        .anyMatch(c -> t.tag().equals(c.tag()) && (t.isDifferent(c) || !t.suite().equals(c.suite()))))
                .map(t -> new Pair<>(t, tests.stream().filter(x -> x.tag().equals(t.tag()))
                        .map(x -> x.id(t.id())).findFirst().orElseThrow()))
                .collect(Collectors.toList());

        if (!modSuiteTests.isEmpty()) {
            updateTestCases(plan, modSuiteTests)
                    .forEach(t -> LOGGER.trace("Remote test #{} updated", t.id()));
        }

        if (!newTests.isEmpty() && createItemsIfAbsent) {
            remoteTests.addAll(
                    createTestCases(plan, newTests).stream()
                            .peek(t -> LOGGER.trace("Remote test #{} created", t.id()))
                            .collect(toList())
            );
        }

        return remoteTests;
    }

    /**
     * Creates a list of test cases for a specified test plan.
     *
     * @param plan      the test plan where the test cases will be created.
     * @param testCases the list of test cases to be created.
     * @return the created test cases, populated with IDs and other metadata.
     */
    public List<TestCase> createTestCases(TestPlan plan, List<TestCase> testCases) {
        BiFunction<String, String, WorkItemOp> newWorkItem = (field, value) -> new WorkItemOp()
                .op(WorkItemOp.Operation.add).path(format("/fields/{}", field)).value(value);

        List<CompletableFuture<TestCase>> futures = testCases.stream().map(t -> {
            List<WorkItemOp> ops = new ArrayList<>();
            ops.add(newWorkItem.apply(TITLE, t.name()));
            ops.add(newWorkItem.apply(TAGS, t.tag()));
            ops.add(newWorkItem.apply(AREA_PATH, path(plan.area())));
            ops.add(newWorkItem.apply(ITERATION_PATH, path(plan.iteration())));
            Optional.ofNullable(t.description()).ifPresent(d -> ops.add(newWorkItem.apply(DESCRIPTION, d)));
            return newRequest()
                    .postCall(response -> response.body().filter(x -> response.statusCode() < 400)
                            .orElseThrow(() -> new WakamitiAzureException("Cannot create test case '{}'. ", t.tag())))
                    .pathParam("type", settings().testCaseType())
                    .queryParam("$expand", "none")
                    .queryParam("suppressNotifications", true)
                    .queryParam("validateOnly", false)
                    .header("Content-Type", "application/json-patch+json")
                    .body(json(ops).toString())
                    .postAsync(project() + "/wit/workitems/${type}")
                    .thenApply(response -> response.body().map(json -> readStringValue(json, "id"))
                            .orElseThrow(() -> new WakamitiException("Cannot create test case. ")))
                    .thenApply(t::id);
        }).collect(toList());
        List<Pair<TestCase, JsonNode>> items = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(x ->
                        futures.stream().map(CompletableFuture::join).map(t -> new Pair<>(t, json(map(
                                "workItem", new WorkItem().id(t.id()),
                                "pointAssignments", List.of(
                                        new PointAssignment().configurationId(settings().configuration()))))))
                ).join().collect(toList());

        try {
            CompletableFuture.allOf(items.stream()
                    .collect(groupingBy(p -> p.key().suite(), mapping(Pair::value, toList())))
                    .entrySet().stream()
                    .map(e -> newRequest()
                            .pathParam("planId", plan.id())
                            .pathParam("suiteId", e.getKey().id())
                            .body(e.getValue().toString())
                            .postAsync(project() + "/testplan/Plans/{planId}/Suites/{suiteId}/TestCase")
                            .thenAccept(response -> response.body().stream()
                                    .flatMap(json -> read(json, "$.value", new TypeRef<List<JsonNode>>() {}).stream())
                                    .forEach(json -> {
                                        String id = readStringValue(json, "$.workItem.id");
                                        int order = Optional.ofNullable(read(json, "$.order", Integer.class))
                                                .orElse(0);
                                        List<PointAssignment> points = read(json, "$.pointAssignments", new TypeRef<>() {});
                                        testCases.stream().filter(t -> t.id().equals(id)).findFirst()
                                                .ifPresentOrElse(t -> t.order(order).pointAssignments(points),
                                                        () -> {
                                                            throw new WakamitiException("Cannot found test case '{}'.", id);
                                                        }
                                                );
                                    })
                            )
                    ).toArray(CompletableFuture[]::new)).get();
        } catch (InterruptedException e) {
            throw new WakamitiException(e);
        } catch (ExecutionException e) {
            throw new WakamitiException(e.getCause());
        }
        return testCases;
    }

    /**
     * Updates a list of test cases with new information.
     *
     * @param plan      the test plan associated with the test cases.
     * @param testCases a list of pairs, where each pair contains the
     *                  current test case and its updated version.
     * @return the updated test cases.
     */
    public List<TestCase> updateTestCases(TestPlan plan, List<Pair<TestCase, TestCase>> testCases) {
        BiFunction<String, String, WorkItemOp> newWorkItem = (field, value) -> new WorkItemOp()
                .op(WorkItemOp.Operation.replace).path(format("/fields/{}", field)).value(value);

        List<TestCase> remove = new LinkedList<>();
        List<TestCase> add = new LinkedList<>();

        CompletableFuture.allOf(testCases.stream().filter(p -> p.key().isDifferent(p.value())).map(p -> {
            List<WorkItemOp> ops = new ArrayList<>();
            TestCase oldT = p.key();
            TestCase newT = p.value();
            if (!oldT.name().equals(newT.name())) {
                ops.add(newWorkItem.apply(TITLE, newT.name()));
            }
            if (!Objects.equals(oldT.description(), newT.description())) {
                ops.add(newWorkItem.apply(DESCRIPTION, newT.description()));
            }
            return newRequest()
                    .pathParam("id", oldT.id())
                    .body(json(ops).toString())
                    .header("Content-Type", "application/json-patch+json")
                    .patchAsync(project() + "/wit/workitems/{id}");
        }).toArray(CompletableFuture[]::new)).join();
        testCases.stream().filter(p -> !p.key().suite().equals(p.value().suite())).forEach(p -> {
            remove.add(p.key());
            add.add(p.value());
        });

        List<CompletableFuture<?>> futures = new LinkedList<>();
        // remove suite relation
        remove.stream().collect(groupingBy(TestCase::suite, mapping(TestCase::id, toList())))
                .entrySet()
                .stream()
                .map(e -> newRequest()
                        .pathParam("planId", plan.id())
                        .pathParam("suiteId", e.getKey().id())
                        .pathParam("testCaseIds", join(e.getValue(), ","))
                        .deleteAsync(project() + "/testplan/Plans/{planId}/Suites/{suiteId}/TestCase/{testCaseIds}"))
                .forEach(futures::add);

        // add suite relation
        add.stream().collect(groupingBy(TestCase::suite, mapping(TestCase::id, toList())))
                .entrySet()
                .stream()
                .map(e -> newRequest()
                        .pathParam("planId", plan.id())
                        .pathParam("suiteId", e.getKey().id())
                        .pathParam("testCaseIds", join(e.getValue(), ","))
                        .postAsync(project() + "/testplan/Plans/{planId}/Suites/{suiteId}/TestCase/{testCaseIds}"))
                .forEach(futures::add);

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new WakamitiException(e);
        }
        return testCases.stream().map(p -> p.key().merge(p.value())).collect(toList());
    }

    /**
     * Creates a new test run within Azure DevOps.
     *
     * @param run the test run to be created.
     */
    public void createRun(TestRun run) {
        newRequest().body(json(run).toString()).post(project() + "/test/runs").body()
                .map(json -> readStringValue(json, "id"))
                .map(run::id)
                .orElseThrow(() -> new WakamitiAzureException("Cannot create test run for plan '{}'", run.plan().id()));
    }

    /**
     * Updates an existing test run with new information.
     *
     * @param run the test run to be updated.
     */
    public void updateRun(TestRun run) {
        newRequest().body(json(run).toString())
                .pathParam("runId", run.id())
                .patch(project() + "/test/runs/{runId}");
    }

    /**
     * Attaches files (e.g., test reports) to a test run.
     *
     * @param run     the test run to which the files will be attached.
     * @param reports a set of file paths representing the reports to be attached.
     */
    public void attachFile(TestRun run, Set<Path> reports) {
        CompletableFuture.allOf(reports.stream().map(report -> {
                    try {
                        return newRequest().body(json(new Attachment()
                                        .attachmentType(Attachment.Type.GeneralAttachment)
                                        .fileName(report.getFileName().toString())
                                        .stream(Base64.getEncoder().encodeToString(Files.readAllBytes(report)))
                                ).toString())
                                .pathParam("runId", run.id())
                                .postAsync(project() + "/test/Runs/{runId}/attachments");
                    } catch (IOException e) {
                        throw new WakamitiException("Error creating attachment", e);
                    }
                }
        ).toArray(CompletableFuture[]::new)).join();
    }

    /**
     * Retrieves test results from a specified test run.
     *
     * @param run the test run for which results are fetched.
     * @return a stream of test results.
     */
    public Stream<TestResult> getResults(TestRun run) {
        return newRequest().pathParam("runId", run.id())
                .getAllPages(project() + "/test/Runs/{runId}/results", new TypeRef<>() {});
    }

    /**
     * Updates a batch of test results with new data.
     *
     * @param run     the test run associated with the test results.
     * @param results the list of test results to be updated.
     */
    public void updateResults(TestRun run, List<TestResult> results) {
        results.forEach(r -> r.startedDate(Util.toZoneId(r.startedDate(), settings().zoneId()))
                .completedDate(Util.toZoneId(r.completedDate(), settings().zoneId()))
                .state(Completed));
        CompletableFuture.allOf(ListUtils.partition(results, MAX_LIST).stream()
                .map(res ->
                        newRequest().pathParam("runId", run.id())
                                .body(json(res).toString())
                                .patchAsync(project() + "/test/Runs/{runId}/results")
                ).toArray(CompletableFuture[]::new)).join();
    }

    /**
     * Creates a new Azure API request object, preconfigured for this service.
     *
     * @return a new instance of AzureApi.
     */
    public AzureApi newRequest() {
        return super.newRequest();
    }

    /**
     * Creates a copy of the current TestPlanService, including configuration and state.
     *
     * @return a copy of this service instance.
     */
    @Override
    public AzureApi copy() {
        AzureApi clone = super.copy();
        clone.tagExtractor = tagExtractor;
        return clone;
    }
}
