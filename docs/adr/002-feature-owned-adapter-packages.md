# 002. Feature-Owned Adapter Packages

Date: 2026-08-01

## Status

Accepted

## Context

The adapter layer mixed unrelated implementations in flat technical packages. Capability aggregation directly imported
configuration properties owned by API security, image upload, and WebDriver adapters. This obscured feature ownership
and allowed adapter implementations to bypass application ports.

## Decision

Each adapter feature owns its application-port implementations, Spring composition, framework integrations, and
feature-local tests. Adapter features communicate only through framework-free `app` ports. `adapter.api` may collaborate
internally between its REST, AsyncAPI, and security transport packages, but it does not own unrelated adapter features.

Persistence is organized vertically below `adapter.db`: each feature owns its service implementation, entities,
repositories, mappers, promotion helpers, and tests. `adapter.db.shared` contains only Spring/JPA helpers genuinely used
by multiple database features.

Capability aggregation is its own `adapter.capability` feature. Property-owning adapters publish immutable capability
contributions through an `app` port; the aggregator validates and orders those contributions without importing property
types from their owners.

## Consequences

- Application ports are the only boundary between adapter features.
- Spring and persistence implementation details remain feature-local.
- Architecture tests enforce adapter-feature isolation while retaining API-internal collaboration.
- New cross-feature adapter dependencies require a redesign around an application port rather than an exception to this
  decision.
