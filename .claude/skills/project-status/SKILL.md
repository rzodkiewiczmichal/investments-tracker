---
name: project-status
description: Produce a Polish HTML status report of the whole project (code, functional and non-functional requirements, GitHub issues, MVP, roadmap, git state) using parallel low-cost agents, highlighting what is in progress, small steps to finish and next small and large steps.
disable-model-invocation: true
---

# Project status report

Output: `temp/status-projektu.html`, self-contained, in Polish, light/dark aware. It's a temp artifact, so never commit it.

## 1. Gather in parallel
Launch these agents in **one message**, all with `model: haiku` and `run_in_background: true`. Every agent is read-only and must return facts with file paths.

| Agent | Scope |
|---|---|
| backend | Packages; endpoints (path + method); use cases; parsers and providers; Flyway list; test counts by type; Cucumber features with scenario counts and tags; TODO/FIXME; stubs and dead code |
| frontend+infra | Routes and components; called endpoints; what the user can do; frontend tests; docker-compose; `dev.sh`; CI jobs; observability |
| FR | Every FR: ID, title, version, status (DONE / PARTIAL / NOT STARTED) with evidence from code, tags and frontend; scope drift (code with no FR) |
| NFR | Every NFR: ID, category, version, status with evidence (build files, CI, config, ArchUnit) |
| GitHub | Via `gh`: epics with sub-issue progress; open issues by epic; recent closed issues and merged PRs; open PRs; inconsistencies (open issues already done, epics done but open, orphan issues) |
| planning/docs | Roadmap versions; ADR index and status; scenario mapping coverage; docs stale vs code (diagrams vs migrations, roadmap vs integrations); last-updated dates |
| git | `git status`; branches and worktrees (squash-merged vs genuinely unmerged: compare against PR `headRefOid`); commit timeline by phase; days since the last commit on main |

## 2. Verify before writing
Haiku reports contradict each other. Resolve every contradiction, and any claim the report depends on, with your own grep, `git` or `gh` checks. Typical traps:
- a table that exists but nothing writes to (e.g. `audit_log`),
- squash-merged branches that look unmerged,
- issues whose code has already landed,
- the status in an ADR file disagreeing with the ADR index,
- FR counts.

Also probe external providers live (`.claude/skills/add-instruments/scripts/check-price.sh CDR.PL AAPL.US`).

## 3. Write the report
Sections, each with status badges (done / partial / missing):
1. Summary: a short TL;DR plus KPI tiles.
2. Timeline, including gaps in activity.
3. Code: backend, frontend, tests, infra.
4. Functional requirements: MVP table, then the other versions, then scope drift.
5. Non-functional requirements: strengths and gaps.
6. GitHub: epics, "on GitHub vs in reality", inconsistencies.
7. Roadmap vs reality.
8. Stale documentation.
9. Work in progress.
10. Small steps to close, as a checklist with effort estimates.
11. Next steps, small and large, plus a recommended order.

Footer: say which agents gathered the data and which facts were verified by hand. Open the file with `open` and give the user a short summary in the terminal.
