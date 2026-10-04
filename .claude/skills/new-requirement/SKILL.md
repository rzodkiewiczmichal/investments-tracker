---
name: new-requirement
description: Add or change a functional requirement (FR), non-functional requirement (NFR) or architecture decision (ADR), keeping roadmap and traceability documents consistent. Use when the user describes new desired behavior, changes scope or version of a requirement, or makes/changes an architectural decision.
disable-model-invocation: true
---

# Add or change a requirement / decision

Documentation rules (single source of truth) are in `.claude/rules/docs.md`.

## FR / NFR
1. **Clarify first.** Ask the user about:
   - behavior and acceptance examples,
   - priority (Must / Should / Could Have),
   - target version,
   - dependencies,
   - exclusions.

   Check `requirements/functional/ubiquitous-language.md` and add any new term there.
2. **Pick the ID.** Use the next free number in the right category block, e.g. 02x for import or 08x for calculations. Grep to make sure it's unused.
3. **Write the entry** in `requirements/functional/functional-requirements.md` (or the NFR file) in the existing format: `templates/fr-template.md`.
4. **Add one row** to the table in `planning/requirements-by-version.md`. That's the only place the ID → version mapping lives.
5. **Update the narrative.** If the requirement changes a version's scope, edit the narrative in `planning/versions-roadmap.md`, referencing IDs rather than copying them.
6. **Write a scenario.** If the requirement is testable, offer to write its scenario with the `bdd-scenario` skill.
7. **Track it.** Offer to create the GitHub issue (and sub-issue under its epic).

## ADR
1. Use the next number in `docs/adr/`. File name: `ADR-0NN-<kebab-title>.md`, following `templates/adr-template.md`.
2. Record the real alternatives that were considered, and the consequences: positive, negative and follow-ups.
3. Add the ADR to the index table in `docs/adr/README.md`, and keep the status consistent between the ADR file and the index.
4. When the ADR supersedes or amends another, update the old ADR's status line and cross-link both.
5. If the decision changes a convention, update the matching `.claude/rules/*.md` in the same change.
