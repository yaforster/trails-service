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

## Manual Deployment

Use this section when the surrounding services are provisioned separately rather than by a local Compose stack. Trails
does not provide production manifests or a supported host-OS matrix. The supplied container runtime uses
`eclipse-temurin:25-jre-noble`; for a non-container deployment, provide a Java 25 runtime supported by the chosen
platform. Linux amd64 is the practical baseline when Chrome, Firefox, and Edge Grid nodes are all required: the Selenium
Edge Linux image is amd64-only. Pin and vet all image tags and base-image updates through your normal platform process.

| Component | Required when | Provisioning requirement |
| --- | --- | --- |
| Trails Service | Always | Run the image built from this repository or the packaged JAR. It listens on Spring Boot's default internal port `8080` unless `SERVER_PORT` changes it. |
| MySQL | Always | Create a persistent database and a least-privilege service account. Configure the datasource values; Liquibase creates and updates the schema, while Hibernate validates it. Back up and restore this data as application state. |
| Selenium Grid Router/Hub | Browser tests run | Provide a Grid endpoint with reachable nodes for every requested browser. Current local reference images are `selenium/hub:4.43.0` and `selenium/node-{chrome,firefox,edge}:4.43.0`; use a compatible, explicitly pinned release in managed environments. |
| Selenium browser nodes | Browser tests run | Size nodes for expected parallel sessions, reserve at least `2g` shared memory for containerized browsers, enable managed downloads, and set node session limits deliberately. No shared download filesystem is required. |
| OIDC/JWT identity provider | `SERVICE_API_SECURITY_OAUTH2_ENABLED=true` | Use an issuer compatible with OAuth2 resource-server JWT validation. Keycloak is one local reference (`quay.io/keycloak/keycloak:26.6.1`), not a requirement; persist and back up its configuration. |
| Reverse proxy / ingress | Public or TLS-exposed API | Terminate TLS and restrict public routes. Set `SERVER_FORWARD_HEADERS_STRATEGY=framework` when the proxy supplies forwarded headers. |

The frontend, documentation site, and Trails Scout are separate clients, not backend startup dependencies. MySQL must be
ready before Trails starts. Start Grid and its browser nodes before accepting browser-test work. Start the identity
provider before enabling OAuth2/JWT validation.

### Network And Firewall Rules

Allow these routes by DNS name or stable address. Keep database, Grid control, and browser-node ports on private
networks; they are not public API endpoints.

| Source | Destination | Port / protocol | Why |
| --- | --- | --- | --- |
| Client or reverse proxy | Trails Service | TLS public port; typically HTTP `8080` behind the proxy | API and health access. |
| Trails Service | MySQL | TCP `3306` | Application data and Liquibase migrations. |
| Trails Service | Selenium Grid Router | TCP `4444` | Remote WebDriver session creation and execution. |
| Selenium node | Grid Hub event bus | TCP `4442`, `4443` | Node registration and Grid events when nodes run on separate hosts. |
| Grid Hub/Distributor | Selenium node advertised endpoint | TCP `5555` by default | Grid sends commands to registered remote nodes. Use unique advertised ports when nodes share a host. |
| Selenium node/browser | Application under test | TCP `80`, `443`, or stage-specific ports | Browser navigation resolves from the node, not from Trails or the operator workstation. |
| Selenium node/browser | Trails browser base URL | Applicable HTTP(S) port | Needed when a loopback stage URL is rewritten to the configured browser-facing address. |
| Trails Service | OIDC issuer or JWK endpoint | TCP `443` or provider-specific port | JWT metadata/key retrieval when OAuth2 is enabled. |
| User browser or frontend | OIDC issuer | TLS public port | Interactive login, when used. |

For co-located Grid components, the event-bus and node routes can remain inside the restricted Grid network. For
cross-host Grid deployments, publish and firewall them explicitly. Do not treat Trails CORS behavior as network access
control: it permits credential-free browser preflight handling, not access to protected API operations.

### Wiring Checklist

1. Configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and, when needed,
   `SPRING_DATASOURCE_DRIVER_CLASS_NAME` from the platform secret store.
2. Set `SERVICE_WEBDRIVERS_GENERAL_GRIDURL` to the Grid Router URL reachable from Trails.
3. Set `SERVICE_WEBDRIVERS_GENERAL_BROWSERBASEURL` to an address reachable from browser nodes when stages use
   `localhost`, `127.0.0.1`, or `::1`. Trails replaces only the scheme and authority, preserving the stage path, query,
   and fragment. Do not use the operator workstation's loopback address.
4. Align `SERVICE_TEST_EXECUTION_MAXIMUM_CONCURRENT_RUNS`,
   `SERVICE_TEST_EXECUTION_RUN_QUEUE_CAPACITY`, and
   `SERVICE_TEST_EXECUTION_MAXIMUM_CONCURRENT_TEST_SETS` with actual Grid capacity. Defaults admit 16 active runs, queue
   64, and run 16 test sets.
5. Leave `SERVICE_API_SECURITY_OAUTH2_ENABLED=false` for an intentionally trusted internal deployment. When enabling it,
   configure exactly one of `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` or
   `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI`, all four Trails role settings, and the applicable role-claim
   paths. Keep CSRF disabled for bearer-token REST clients unless cookie/session flows are deliberately introduced.

An issuer URL used by a browser can differ from the JWK URL used by Trails if each resolves the provider through a
different network. Both must be valid from their respective callers. Store database, identity-provider, and service
secrets in the deployment platform; do not place them in repository files or image layers.

### Startup Verification

1. Verify DNS, TLS, and firewall routes from each source listed above.
2. Confirm MySQL connectivity, then start Trails and allow Liquibase to validate or migrate the schema.
3. Confirm Grid reports all required browser nodes at its `/status` endpoint and create one session per required browser.
4. Check `GET /actuator/health` and `GET /api/capabilities` through the intended ingress policy.
5. When OAuth2 is enabled, validate JWT key retrieval and one token for every required Trails role.
6. Run a browser test against a non-loopback stage and, when relevant, a loopback stage using the configured browser base
   URL. Verify managed-download behavior without mounting a host download directory.

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
