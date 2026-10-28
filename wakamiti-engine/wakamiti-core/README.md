# Wakamiti Core

`wakamiti-core` is the execution engine behind the launchers. It is the module that turns configuration plus test resources into a plan, executes that plan and coordinates contributors.

## Responsibilities

- build `PlanNode` trees from configured resource types
- load contributors and step providers
- execute plans and collect `Result` plus serialized snapshots
- resolve external modules for launcher-driven runs
- invoke report contributors after execution

## When to use it directly

Most users should enter Wakamiti through one of these modules instead:

- `wakamiti-junit`
- `wakamiti-maven-plugin`
- `wakamiti-launcher`

Use `wakamiti-core` directly only when you need to embed the engine in a custom host application.

## Main concepts

### Plan model

Wakamiti represents a test plan as a tree of nodes:

- `Aggregator`
- `Test Case`
- `Step Aggregator`
- `Step`
- `Virtual Step`

This model allows the engine to transform or redefine the plan before execution while preserving the semantics of the original resources.

### Lifecycle hook scenarios in Gherkin

`wakamiti-core` supports one setup hook and one teardown hook per feature. Declare them as regular Gherkin scenarios
with the reserved tags `@Before` and `@After`:

```gherkin
Feature: Manage the test fixture

  @Before
  Scenario: Prepare the fixture
    Given the fixture is ready

  Scenario: Exercise the feature
    When the operation is executed
    Then the expected result is returned

  @After
  Scenario: Clean up the fixture
    Given the fixture is removed
```

The engine runs the `@Before` scenario before the functional scenarios and the `@After` scenario after them. Both hooks
are feature-level lifecycle scenarios, not functional test cases.

#### Execution and failure rules

- Hooks do **not** receive `Background` steps.
- If any hook step finishes with a result other than `PASSED`, its remaining steps are marked as `SKIPPED`.
- A non-successful `@Before` marks every functional scenario in the feature as `SKIPPED`; the `@After` hook still runs.
- The `@After` hook runs after a feature has started, even when its `@Before` or a functional scenario failed.
- Hooks are marked as `SKIPPED` when tag filtering leaves the feature without an executable functional scenario.
- `stopExecutionOnError` still stops remaining functional scenarios and later features only after an `ERROR`. It does not
  cancel the `@After` hook of the feature already in progress.

#### Results and reports

Hook scenarios are visible in execution output and reports. Their result contributes to the aggregate result of their
parent feature, but hooks are excluded from functional test-case counts and are not treated as strict functional
test-case IDs.

### Configuration

Core configuration is expressed through the `wakamiti.*` namespace, typically in `wakamiti.yaml`. Examples include:

- `resourceTypes`
- `resourcePath`
- `outputFilePath`
- `launcher.modules`
- plugin-specific namespaces such as `rest.*`, `database.*`, `htmlReport.*`

## Programmatic entry point

At the engine level the usual flow is:

1. build or read a `Configuration`
2. create the plan through `Wakamiti.instance().createPlanFromConfiguration(...)`
3. execute it through `Wakamiti.instance().executePlan(...)`
4. optionally call `generateReports(...)`

If you need a supported integration surface rather than a custom embed, use the launcher-specific modules instead of binding to `wakamiti-core` directly.
