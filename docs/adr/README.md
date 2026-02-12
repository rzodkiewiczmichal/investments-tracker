# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records for the Investment Tracker project.

## What is an ADR?

An Architecture Decision Record (ADR) captures an important architectural decision made along with its context and consequences.

## ADR Format

We use the Michael Nygard format with the following structure:

- **Status**: Proposed, Accepted, Deprecated, Superseded
- **Context**: The issue motivating this decision
- **Decision**: The change we're proposing or have agreed to
- **Consequences**: What becomes easier or more difficult

## ADR List

### Domain & Architecture

| ID | Title | Status | Date |
|----|-------|--------|------|
| [ADR-001](ADR-001-aggregate-boundaries.md) | Aggregate Boundaries | Accepted | 2026-01-04 |
| [ADR-002](ADR-002-value-objects-and-entities.md) | Value Objects and Entities | Accepted | 2026-01-04 |
| [ADR-003](ADR-003-domain-vs-application-services.md) | Domain vs Application Services | Accepted | 2026-01-04 |
| [ADR-004](ADR-004-package-structure.md) | Package Structure | Accepted | 2026-01-04 |
| [ADR-005](ADR-005-database-schema.md) | Database Schema Design | Accepted | 2026-01-04 |
| [ADR-006](ADR-006-money-representation.md) | Money Representation | Accepted | 2026-01-04 |
| [ADR-018](ADR-018-domain-model-implementation-rules.md) | Domain Model Implementation Rules | Accepted | 2026-02-01 |
| [ADR-019](ADR-019-factory-and-with-methods.md) | Factory and With Methods | Accepted | 2026-02-01 |
| [ADR-022](ADR-022-use-case-dependencies.md) | Application Core Operates on Domain Model Only | Accepted | 2026-02-01 |
| [ADR-023](ADR-023-cross-aggregate-data-access.md) | Cross-Aggregate Data Access Patterns | Accepted | 2026-02-02 |
| [ADR-024](ADR-024-domain-services-cross-aggregate-calculations.md) | Domain Services for Cross-Aggregate Calculations | Accepted | 2026-02-02 |
| [ADR-025](ADR-025-repository-adapter-single-aggregate.md) | Repository Adapters Handle Single Aggregate Only | Accepted | 2026-02-02 |
| [ADR-026](ADR-026-application-layer-orchestration.md) | Application Layer Orchestrates Cross-Aggregate Operations | Accepted | 2026-02-02 |

### Infrastructure & Dependencies

| ID | Title | Status | Date |
|----|-------|--------|------|
| [ADR-008](ADR-008-dependency-management.md) | Dependency Management | Accepted | 2026-01-05 |
| [ADR-014](ADR-014-docker-compose-configuration.md) | Docker Compose Configuration | Accepted | 2026-01-10 |
| [ADR-015](ADR-015-otlp-observability-strategy.md) | OTLP Observability Strategy | Accepted | 2026-01-10 |
| [ADR-016](ADR-016-database-migration-strategy.md) | Database Migration Strategy | Accepted | 2026-01-10 |
| [ADR-017](ADR-017-transaction-boundaries.md) | Transaction Boundaries | Accepted | 2026-01-10 |
| [ADR-027](ADR-027-immutable-domain-vs-hibernate-entity-tracking.md) | Immutable Domain vs Hibernate Entity Tracking | Accepted | 2026-02-08 |
| [ADR-028](ADR-028-spring-data-jdbc-persistence.md) | Spring Data JDBC as Persistence Mechanism | Accepted | 2026-02-08 |
| [ADR-029](ADR-029-local-dev-startup-script.md) | Local Development Startup Script | Accepted | 2026-02-08 |
| [ADR-030](ADR-030-external-financial-data-providers.md) | External Financial Data Providers | Accepted (revised) | 2026-02-12 |

### REST API

| ID | Title | Status | Date |
|----|-------|--------|------|
| [ADR-009](ADR-009-rest-api-structure.md) | REST API Structure | Accepted | 2026-01-08 |
| [ADR-010](ADR-010-error-handling-strategy.md) | Error Handling Strategy | Accepted | 2026-01-08 |
| [ADR-011](ADR-011-data-validation-strategy.md) | Data Validation Strategy | Accepted | 2026-01-08 |

### Testing

| ID | Title | Status | Date |
|----|-------|--------|------|
| [ADR-012](ADR-012-test-architecture.md) | Test Architecture | Accepted | 2026-01-09 |
| [ADR-013](ADR-013-mock-vs-real-dependencies.md) | Mock vs Real Dependencies | Accepted | 2026-01-09 |

### Frontend

| ID | Title | Status | Date |
|----|-------|--------|------|
| [ADR-018](ADR-018-angular-19-frontend-framework.md) | Angular 19 Frontend Framework | Accepted | 2026-01-31 |
| [ADR-019](ADR-019-frontend-project-structure.md) | Frontend Project Structure | Accepted | 2026-01-31 |
| [ADR-020](ADR-020-primeng-ui-framework.md) | PrimeNG UI Framework | Accepted | 2026-01-31 |
| [ADR-021](ADR-021-frontend-state-management.md) | Frontend State Management | Accepted | 2026-01-31 |

## Process

1. **Create**: When making a significant architectural decision, create a new ADR file
2. **Number**: Use sequential numbering (ADR-001, ADR-002, etc.)
3. **Discuss**: Share with team for review and feedback
4. **Accept**: Mark as "Accepted" when decision is finalized
5. **Implement**: Use ADR as guide during implementation
6. **Update**: If decision changes, create new ADR and mark old one as "Superseded"

## References

- [Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions) by Michael Nygard
- [ADR GitHub Organization](https://adr.github.io/)
