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
