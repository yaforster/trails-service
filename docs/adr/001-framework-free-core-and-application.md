# 001. Framework-Free Core And Application Layers

Date: 2026-08-01

## Status

Accepted

## Context

The `core` and `app` layers exposed Spring Data, Spring HTTP, Spring resources, HATEOAS, Selenium, and PDFBox types.
This made domain behavior dependent on transport, persistence, browser automation, and document-processing frameworks.

## Decision

`core` and `app` depend only on Trails code, the JDK, and approved programming utilities: Lombok, Guava, Apache
Commons, and MapStruct. Framework and infrastructure functionality remains in adapters.

Core browser actions use framework-free browser, download, and document ports. `adapter.test.webdriver` provides
Selenium Grid and PDFBox implementations. Database and API adapters translate Spring pagination, resources, HTTP statuses, and
HATEOAS values at their boundaries.

`LayeredArchitectureTest` enforces this dependency allowlist.

## Consequences

- Core action behavior is testable with fake ports without Selenium or PDFBox.
- Application service contracts no longer expose Spring types.
- New core or application dependencies require an explicit decision and an allowlist update.
- Adapters own framework-specific failure handling and resource cleanup.
