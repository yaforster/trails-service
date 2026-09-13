# Trails

Trails is a Spring Boot service for modeling applications, stages, UI elements, test plans, browser-driven test
execution, and persisted test results. It exposes REST/HATEOAS endpoints, generated from the OpenAPI contract, and uses
Selenium Grid for remote browser execution.

## Project Shape

- `core`: domain records, test execution model, feature-grouped browser actions, shared value classes, and validation
  helpers.
- `app`: service interfaces used by adapters.
- `adapter`: REST controllers, database implementations, security integration, capability reporting, printing, and
  Selenium/WebDriver adapters.
- `src/main/resources/api`: OpenAPI and AsyncAPI contracts.
- `src/main/resources/db/changelog`: Liquibase database migrations.

Architecture is guarded by `LayeredArchitectureTest`. Keep dependencies flowing inward: adapters may depend on `app`
and `core`; `core` must not depend on adapters.

## Runtime Configuration

Supply runtime settings through standard Spring Boot environment variables or your deployment platform's secret store.
This repository publishes no local environment file or Compose stack. The service requires an externally managed MySQL
database and Selenium Grid; OAuth2/JWT validation is optional.

Important settings:

- Database: configure the `SPRING_DATASOURCE_*` values for MySQL.
- Selenium Grid: configure `SERVICE_WEBDRIVERS_GENERAL_GRIDURL`.
- Browser-local URLs: configure `SERVICE_WEBDRIVERS_GENERAL_BROWSERBASEURL` when stages point to `localhost`. Browser
  nodes run in their own container or host, so their `localhost` is not the service host.
- Managed downloads: browser downloads are retrieved through Selenium's managed download API, not through a shared
  filesystem path. Tune `SERVICE_WEBDRIVERS_GENERAL_FILEDOWNLOAD_TIMEOUTMILLIS` if download-heavy tests are flaky.
- User images: configure `SERVICE_FILES_USER_IMG_MAX_SIZE`, `SERVICE_FILES_USER_IMG_MAX_WIDTH`, and
  `SERVICE_FILES_USER_IMG_MAX_HEIGHT` to limit uploaded profile pictures and element screenshots. Omitted, blank, or
  zero limits use the compatible defaults of 5 MiB and 1920 pixels for each dimension; malformed sizes and overflowing
  dimensions prevent startup.
- Test execution: Trails admits 16 active test runs and queues 64 more by default. It executes at most 16 browser test
  sets globally. Tune `SERVICE_TEST_EXECUTION_MAXIMUM_CONCURRENT_RUNS`,
  `SERVICE_TEST_EXECUTION_RUN_QUEUE_CAPACITY`, and `SERVICE_TEST_EXECUTION_MAXIMUM_CONCURRENT_TEST_SETS` to match Grid
  capacity.
- Demo pages: set `SERVICE_SERVE_DEMO_RESOURCES=true` only when you want the bundled static demo pages served by the
  backend.
- OAuth2: set `SERVICE_API_SECURITY_OAUTH2_ENABLED=true` only when this service should validate JWT bearer tokens
  itself.

Never commit environment files, credentials, machine paths, or IDE configuration. Private local orchestration belongs
outside this public repository.

### Configuration Safety

Settings are grouped by the feature that uses them, while retaining the same YAML keys and environment-variable names.
In practical terms, changing a browser setting cannot accidentally alter security behavior, and invalid image limits or an
incomplete enabled OAuth2 setup stop the service during startup rather than causing a failure later during a test run.

## Generated Contracts And Database

The REST contract lives in `src/main/resources/api/hateoas.yaml`. Controllers implement generated interfaces and use
generated DTOs from `target/generated-sources/openapi`, so API shape changes should start in the OpenAPI file.

Database schema changes are Liquibase migrations under `src/main/resources/db/changelog`. Add new change sets to the
versioned changelog directory and include them from `db.changelog-master.yaml`.

## Selenium Execution

Trails creates remote browser sessions through Selenium Grid. Session creation is protected with Resilience4j retry,
circuit breaker, and per-browser bulkheads:

- `seleniumChrome`
- `seleniumFirefox`
- `seleniumEdge`

This protection applies only to remote session creation. Individual browser actions and whole test runs are not retried,
because clicks, form submissions, uploads, and application-side effects are not generally safe to repeat.

### Resize viewport action

`RESIZE_VIEWPORT` changes current browser session any number of times in one test plan. Width and height are required
positive signed 32-bit integers. Target is exact visible CSS viewport `window.innerWidth` × `window.innerHeight`, not
outer window dimensions. Trails measures current inner viewport, makes one compensated outer-window `setSize` attempt,
then measures once. No correction or retry occurs. Unsupported or clamped nodes produce a technical failure containing
requested viewport, last measured viewport (or `unavailable`), and cause. Existing explicit waits remain responsible for
waiting for application reflow.

For an opt-in Chrome, Edge, and Firefox Grid smoke test, provide a loopback-reachable Selenium Hub and run:

```powershell
$env:TRAILS_RESIZE_VIEWPORT_GRID_URL = 'http://127.0.0.1:4444'
.\mvnw.cmd -Dit.test=ResizeViewportGridIT verify
```

When `TRAILS_RESIZE_VIEWPORT_GRID_URL` is absent, `ResizeViewportGridIT` skips. When supplied, session-start failures
fail test; each browser must report exact success or technical failure with complete diagnostic.

Set each browser bulkhead close to the matching browser capacity in the Grid. A full bulkhead, open circuit, or
exhausted retry causes that browser path to be skipped using the normal unavailable-browser result model.

## OAuth2/JWT Security

Trails can run unsecured or as an OAuth2 resource server. The switch is `service.api.security.oauth2.enabled`.

- `false`: the default local/internal mode. API requests are permitted, and internal user/profile beans are not
  registered.
- `true`: `/api/**` and `/user/**` require a valid JWT bearer token. Static demo resources remain public when enabled.

When OAuth2 is enabled:

- configure exactly one JWT validation source: `issuer-uri` or `jwk-set-uri`;
- configure every Trails role value: test manager, tester, admin, and deployment reporter;
- keep `SERVICE_API_SECURITY_CSRF_ENABLED=false` for bearer-token REST clients unless browser session/cookie
  interaction is intentionally introduced;
- role extraction is handled by `JWTClaimConverter` using configurable claim paths such as
  `realm_access.roles` and `resource_access.*.roles`.

CI/CD deployment reporting uses a separately configured deployment reporter role. This allows deployment tokens to use a
different claim layout from human user tokens while still allowing the admin role as an override.

## CORS

Trails processes CORS preflight requests for every endpoint without browser credentials. CORS does not grant API access:
the configured OAuth2 authorization rules continue to protect the API operations declared by the OpenAPI contract.

## Demo Resources

Static demo resources are local-development helpers and are served only when `SERVICE_SERVE_DEMO_RESOURCES=true`.

Current pages include:

- `index.html`: navigation hub, capabilities viewer, and Keycloak token helper.
- `controller-crud-demo.html`: CRUD flows, profile/avatar calls, and deployment reporting.
- `hateoas-navigation-demo.html`: HATEOAS link traversal.
- `test-execution-demo.html`: test execution submission and completion events.
- `dummy.html`: browser-testable sample page.

## Adding A New Action Type

Action mapping is intentionally variant-based for both REST DTOs and persistence entities. `ActionDetailsMapper` and
`ActionEntityMapper` are registry dispatchers. Do not add central `switch` branches for new action details.

Execution actions are grouped by feature under `core.test.action`:

- `browser`: generic browser/page actions such as website switching and explicit waits.
- `document`: downloaded document text checks, plain-text extraction, PDF matching, and PDF visual proof rendering.
- `download`: downloaded-file checks.
- `element`: element interactions, element checks, locators, and element value checks.
- `storage`: cookie, local storage, and session storage checks.
- `value`: value-computation instructions.
- `viewport`: viewport movement actions and related enums.

Checklist:

- Add or update the execution action under the matching `core.test.action.<feature>` package.
- Add focused tests for Selenium behavior and produced result messages.
- Update `src/main/resources/api/hateoas.yaml` so OpenAPI generates the new `ActionDetailsDTO` type and discriminator.
- Add a package-private REST mapper variant in `adapter.api.rest.action.mapper` extending
  `AbstractActionDetailsVariantMapper<DomainAction, DetailsDTO>`.
- Put REST-specific conversion in the REST variant mapper. Use `ActionDetailsMappingContext` for shared conversions
  such as element ids, positions, value-computation instructions, and default handling.
- Add or update the persistence details entity under `adapter.db.testplan.entity.details`.
- Add a Liquibase changelog when the new action requires schema changes.
- Add or update the persistence detail mapper under `adapter.db.testplan.mapper.detail`.
- Add a package-private entity mapper variant in `adapter.db.testplan.mapper.detail` extending
  `AbstractActionEntityVariantMapper<DomainAction, DetailsEntity>`.
- Add a promotion copy variant under `adapter.db.testplan.promotion` extending
  `AbstractActionDetailsCopyVariant<DetailsEntity>`.
- Update validation and controller tests when the action has required fields, defaults, or hierarchy constraints.
- Cover DTO mapping in `ActionDetailsMapperTest`.
- Cover entity mapping in `ActionEntityMapperTest`.
- Cover promotion copying in the promotion service tests.
- Run the action mapper tests, entity mapper tests, promotion tests, architecture test, and domain action tests before
  committing.

The intended extension model is: add small action-specific variants in predictable packages, while keeping central
dispatchers unchanged.

## Container Image

The Dockerfile builds the service JAR from this repository's source. Build an image with:

```powershell
docker build -t trails-service .
```

Run the image with externally managed database, Grid, and optional identity-provider endpoints. Supply its settings as
container environment variables or platform-managed secrets.

Spring Boot property precedence still applies in containers:

```text
command line args > JVM -D properties > environment variables > application.yaml
```
