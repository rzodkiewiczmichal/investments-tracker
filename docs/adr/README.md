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

| ID | Title | Status | Date |
|----|-------|--------|------|
| [ADR-001](ADR-001-aggregate-boundaries.md) | Aggregate Boundaries | Accepted | 2026-01-04 |
| [ADR-002](ADR-002-value-objects-and-entities.md) | Value Objects and Entities | Accepted | 2026-01-04 |
| [ADR-003](ADR-003-domain-vs-application-services.md) | Domain vs Application Services | Accepted | 2026-01-04 |
| [ADR-004](ADR-004-package-structure.md) | Package Structure | Accepted | 2026-01-04 |

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
