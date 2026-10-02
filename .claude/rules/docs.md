---
paths:
  - "requirements/**"
  - "planning/**"
  - "docs/**"
  - "README.md"
---
# Documentation rules

**Single source of truth:** each fact lives in exactly one document. Other documents cross-reference it ("See requirements-by-version.md for ..."). Never copy ID lists or tables between docs.
- `requirements-by-version.md` holds the flat ID → version lookup. `versions-roadmap.md` holds the narrative and references that lookup.
- Allowed repetition: summary counts, a different view of the same data, and context a document needs to stand on its own.
- Warning signs: copied ID lists, repeated descriptions, the same table in two places, or one change that needs edits in several files.

**ADRs:** one decision per file in `docs/adr/`. Keep the index in `docs/adr/README.md` updated. When a decision changes, revise or supersede the ADR instead of silently diverging from it.

**Keep in sync with code:** `docs/diagrams/*` (domain model, DB schema), `docs/api/openapi.yaml`, and the requirement and scenario mapping in `planning/`.
