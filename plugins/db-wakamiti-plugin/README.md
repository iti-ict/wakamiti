Wakamiti :: Database plugin
====================================================================================================

## Usage
- [English](./docs/en.md)
- [Spanish](./docs/es.md)

## Contributors

### Technical overview

This plugin is implemented as three Wakamiti extensions:
- `DatabaseStepContributor` (`StepContributor`): exposes all Gherkin database steps.
- `DatabaseConfigContributor` (`ConfigContributor`): maps `database.*` properties to runtime behavior.
- `DriverConnectionManager` (`ConnectionManager`): default JDBC connection provider.

### Execution model

- Configuration is loaded first. Connection parameters can come from `database.connection.*` (default alias) or `database.datasource.{alias}.*` (named aliases).
- Connections are created lazily through `ConnectionProvider`, and can be health-checked with an engine-specific query (`DatabaseType`).
- Every step delegates to `DatabaseSupport`, which centralizes SQL execution, inserts/deletes, assertions, async waits, and cleanup orchestration.

### SQL and metadata pipeline

- SQL strings are parsed with JSqlParser (`SQLParser`) to split scripts, normalize statements, and generate `SELECT/INSERT/UPDATE/DELETE` statements used by the plugin internals.
- `Database` wraps JDBC operations and caches metadata per URL (table names, columns, primary keys, and types) to reduce metadata round-trips.
- Value coercion is type-aware (`processData`) and applies per-database SQL formatting (`DatabaseType` + `SqlFormat`).

### Data ingestion and assertions

- Supported dataset sources are in-memory tables (`DataTableDataSet`), CSV (`CsvDataSet`), and XLSX (`OoxmlDataSet` via Apache POI).
- A configurable `nullSymbol` is converted to SQL `NULL` consistently across all dataset types.
- Assertions can run synchronously or asynchronously (Awaitility). Async steps poll until timeout and then fail with detailed diagnostics.
- For mismatch diagnostics, the plugin computes a “closest record” using Levenshtein similarity; optional Lucene preselection (`database.similarSearch.lucene.*`) reduces candidate search cost.

### Cleanup and lifecycle

- Cleanup actions are stored as a stack of deferred operations and executed in teardown (`@TearDown(order = 1)`), allowing rollback-like behavior after scenario completion.
- SQL statement visitors capture pre/post images of changed rows to build compensating actions for `INSERT`, `UPDATE`, `DELETE`, and `TRUNCATE` flows.
- Resource cleanup is explicit: Lucene indexes and JDBC connections are closed in teardown (`@TearDown(order = 2)`), then internal caches/maps are cleared to avoid scenario leakage.
