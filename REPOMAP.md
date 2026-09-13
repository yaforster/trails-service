# Trails service repository map

## Ownership and entry points

| Area | Source of truth | Responsibility |
| --- | --- | --- |
| Bootstrap | `src/main/java/io/github/yaforster/trails/TrailsService.java` | Spring Boot application bootstrap. |
| Core | `src/main/java/io/github/yaforster/trails/core` | Domain records, shared values, validation, and reusable behavior. Feature packages include `data`, `definition`, `deletion`, `persisted`, `print`, `test`, and `user`; action variants live below `core/test/action/{browser,document,download,element,value}`. |
| Application | `src/main/java/io/github/yaforster/trails/app` | Use-case contracts, feature service ports in `app/services`, capability-contribution contracts, and shared mapper, time, and event contracts consumed by adapters. |
| API adapters | `src/main/java/io/github/yaforster/trails/adapter/api` | REST, AsyncAPI, and security transport integration. |
| Infrastructure adapters | `src/main/java/io/github/yaforster/trails/adapter/{capability,db,print,runtime,test,testdata}` | Capability projection, feature-owned persistence and test-plan promotion, HTML-to-PDF printing, system time, test execution, Selenium/Grid integration, and test-data resolution. |
| API contracts | `src/main/resources/api/hateoas.yaml`, `src/main/resources/api/asyncapi.yaml` | REST OpenAPI and AsyncAPI source specifications. |
| Generated REST API | `target/generated-sources/openapi` | OpenAPI output generated from `hateoas.yaml` by the Maven plugin configured in `pom.xml`; do not edit. |
| AsyncAPI transport code | `src/main/java/io/github/yaforster/trails/adapter/api/asyncapi` | AsyncAPI event endpoints, mappers, and transport models. Keep these aligned with `asyncapi.yaml`. |
| Database schema | `src/main/resources/db/changelog` | Additive Liquibase migrations, currently through `v1/023-add-resize-viewport-action.yaml`; never rely on Hibernate DDL for production schema changes. |
| Runtime configuration | `src/main/resources/application.yaml`, `README.md` | Spring defaults, logging, and documented externally supplied operator configuration. |
| Deployment | `Dockerfile` | Source-built container image; private local orchestration is owned outside this repository. |
| Print templates | `src/main/resources/templates` | Thymeleaf HTML templates and fragments rendered to PDF by `adapter/print`. |
| Architecture decisions | `docs/adr` | Accepted boundary decisions: framework-free core/application and feature-owned adapter packages. |
| Tests | `src/test/java` | Unit, integration, architectural, naming, and application-service arity verification. |

## Filesystem tree

```text
trails-service/
|- src/
|  |- main/
|  |  |- java/io/github/yaforster/trails/
|  |  |  |- TrailsService.java                 Spring Boot bootstrap
|  |  |  |- core/                              Domain model and shared behavior
|  |  |  |  `- test/action/{browser,document,download,element,value}/
|  |  |  |- app/                               Use-case contracts and ports
|  |  |  |  `- services/                        Feature persistence, query, execution, print, and capability ports
|  |  |  `- adapter/                           External-system implementations
|  |  |     |- api/                            REST, AsyncAPI, and security
|  |  |     |  |- rest/                         Resource controllers and HATEOAS
|  |  |     |  |- asyncapi/                     Test-execution event transport
|  |  |     |  |- security/                     OAuth2/JWT authentication and authorization
|  |  |     |- capability/                     Runtime capability projection
|  |  |     |- db/                             JPA persistence grouped by implemented features
|  |  |     |  |- {application,artifact,deployment,stage,testdata,testplan,user}/
|  |  |     |  |  `- testplan/{entity,mapper,promotion,repo}/
|  |  |     |  |- element/screenshot/             Elements and element screenshots
|  |  |     |  |- result/{action,run,testset,testpath}/ Result hierarchy persistence and queries
|  |  |     |  `- shared/                        Shared Spring/JPA helpers only
|  |  |     |- print/                          PDF export implementation
|  |  |     |- runtime/time/                   System clock implementation
|  |  |     |- test/
|  |  |     |  |- execution/                    Test-run orchestration and arrangement
|  |  |     |  `- webdriver/                    Selenium/Grid, browser, downloads, PDFBox
|  |  |     `- testdata/                       Test-data value-reference resolution
|  |  `- resources/
|  |     |- api/                               OpenAPI and AsyncAPI source contracts
|  |     |- db/changelog/                      Liquibase migrations
|  |     |- templates/                         PDF HTML templates
|  |     |- application.yaml                   Runtime defaults
|  `- test/java/                               Unit, integration, and architecture tests
|- pom.xml                                      Maven build, generators, test plugins
|- Dockerfile                                   Source-built container image
|- docs/adr/                                    Accepted architecture decisions
|- README.md                                    Operator and development guidance
`- AGENTS.md and CONTINUITY.md                  Repository rules and current context
```

## Adapter responsibilities

`adapter` implements the application ports at the service boundary. It translates between the domain/application model
and external concerns without allowing one adapter to call another directly: adapters depend on `app` contracts, while
`core` remains independent of adapter code.

| Area | Responsibilities and supported use cases |
| --- | --- |
| `adapter/api` | Exposes HATEOAS REST resources for applications, stages, elements, test plans, test data, deployments, artifacts, user profiles, execution results, and PDF exports. It validates/maps transport DTOs, assembles links, publishes and receives AsyncAPI test-execution events, and enforces OAuth2/JWT roles. |
| `adapter/capability` | Aggregates adapter-published capability contributions into the executable capability projection. |
| `adapter/db` | Persists and retrieves the modeled hierarchy, test definitions, uploads, deployments, and execution results through JPA. Implemented feature packages own application, artifact downloaded-file storage/retrieval, deployment, element/screenshot, result/action, result/run, result/testset, result/testpath, stage, test-data, test-plan, and user persistence. The test-plan aggregate owns action persistence, next-action reference validation, and promotion copying. |
| `adapter/print` | Renders test-run and test-set outcomes as streamed PDF exports from HTML templates, including result hierarchy and screenshot content. |
| `adapter/runtime/time` | Supplies the system clock through the application time port. |
| `adapter/test/execution` | Arranges validated test plans into executable paths and manages bounded test-run and test-set execution. |
| `adapter/test/webdriver` | Implements framework-free browser/document ports with Selenium Grid and PDFBox, rewrites loopback stage URLs for containers, and collects browser-driven outcomes/downloads. |
| `adapter/testdata` | Resolves stored test-data references through the application test-data port. |

## Architectural boundaries

- `core` must not depend on `app` or `adapter`.
- `app` may depend on `core`, but not on `adapter`.
- `adapter` implements `app` contracts and must not bypass them to call another adapter.
- Generated REST transport DTOs remain inside `adapter.api`.
- Persistence entities, mappers, and repositories remain inside `adapter.db`.
- `core` and `app` use Trails code, the JDK, and only approved programming utilities; framework and infrastructure
  libraries are adapter-owned and the allowlist is enforced by `LayeredArchitectureTest`.

These rules are executable in `src/test/java/io/github/yaforster/trails/LayeredArchitectureTest.java`.

## Change routing

- A transport behavior change starts with the matching API contract, then its API adapter, application contract, and
  persistence/domain implementation as applicable.
- A persisted model change requires an additive Liquibase migration plus mapping and promotion/retrieval coverage.
- A new action variant crosses core action modeling, API contract/mapping, persistence details/mapping, promotion, and
  focused tests.
- A test-plan promotion change belongs in `adapter/db/testplan`; copy strategy lives in `promotion`. Preserve source
  action-reference validation before copying or resolving target IDs.
- An operator-facing setting changes the configuration sources and `README.md` together.

## Verification

- Unit tests: `./mvnw clean test` (`.\\mvnw.cmd clean test` on Windows).
- Package: `./mvnw clean package` (`.\\mvnw.cmd clean package` on Windows).
- Full unit and integration verification: `./mvnw clean verify` (`.\\mvnw.cmd clean verify` on Windows). `*IT.java`
  integration tests run through Failsafe.
- Local application: `./mvnw spring-boot:run` (`.\\mvnw.cmd spring-boot:run` on Windows).
- Executable architecture and naming rules: `LayeredArchitectureTest`, `EntityNamingConventionTest`,
  `ServiceNamingConventionTest`, and `AppServiceMethodArityTest` under
  `src/test/java/io/github/yaforster/trails`.
