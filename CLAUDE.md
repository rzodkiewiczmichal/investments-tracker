# Investment Tracker - Project Context for Claude

**PRIORITY 0:** Include in claude.md only the information I explicitly share, do not include anything else, do not guess, do not try to anticipate.

## Project Overview
**Name:** Investment Tracker
**Purpose:** Application for private usage to track investments done in multiple different broker accounts

## Project Goals

### More Important Goal
Practice development of clean modern architecture clean code Java application proofing:
- Domain Driven Design skills
- Cucumber testing skills
- Backend development skills

### Minor Goal
Have working application for my usage

## Development Workflow

### Claude's Role
Claude will be used for all phases of software lifecycle:
- Defining requirements
- Creating epics, tasks etc
- Discovering domains
- Designing architecture
- Implementing the application

Claude Code will not be just agentic code help - will use all handy MCP servers for variety of automations (e.g., GitHub MCP server is configured)

## Project Location
GitHub repository cloned in this directory

## Output Guidelines

Claude must use one of three output types based on the nature of the response:

### 1. Normal Artifact
**Definition:** Files intended to stay in the project permanently, be committed and pushed to git.

**Examples:**
- Formal requirements documents (functional-requirements.md, non-functional-requirements.md)
- Domain documentation (ubiquitous-language.md, user-personas.md)
- Planning documents (VERSION-ROADMAP.md)
- Feature files (*.feature)
- Source code files
- Architecture documentation

**Location:** Appropriate directory in project structure

### 2. Temp Artifact
**Definition:** Files in `/temp` directory used when output is long enough to be unreadable in terminal. Intended to be read by user and deleted afterwards.

**Examples:**
- Analysis documents (like PROPOSED-STRUCTURE.md)
- Long comparisons or evaluations
- Draft proposals for discussion
- Verbose reports or summaries
- Exploration documents

**Location:** `/temp` directory (must be created if it doesn't exist)

### 3. Terminal Response
**Definition:** Direct response in terminal without creating a file. For short questions and answers.

**Examples:**
- Brief answers to questions
- Confirmations
- Short explanations
- Status updates
- Simple guidance

**Location:** No file created

### Decision Criteria

Claude should judge which type to use based on:
- **Length:** Short (terminal), Medium-Long (temp), Permanent value (normal)
- **Purpose:** Exploration/draft (temp), Formal documentation (normal), Quick answer (terminal)
- **Lifespan:** Temporary (temp), Permanent (normal), Immediate (terminal)
- **Commit-worthiness:** Should this be in git history? (yes = normal, no = temp or terminal)

## Documentation Quality Rules

### Single Source of Truth Principle

**Rule:** Each piece of information should have exactly ONE authoritative location in the project.

**Rationale:** Redundancy creates maintenance burden - when information changes, all duplicates must be updated, leading to inconsistencies.

**Guidelines:**

1. **Before creating content:**
   - Check if similar information exists elsewhere
   - If it exists, reference it rather than duplicating it

2. **When information is needed in multiple places:**
   - Choose ONE authoritative source
   - Other documents should cross-reference: "See [document-name.md] for [specific information]"
   - Example: ✅ "See requirements-by-version.md for complete requirement-to-version mapping"
   - Anti-example: ❌ Copying 20 requirement IDs into multiple documents

3. **Acceptable exceptions:**
   - Summary statistics in multiple places (e.g., "Total: 57 requirements")
   - Different perspectives of same data (e.g., static table vs. progressive timeline)
   - Essential context needed for document to be self-contained

4. **When refactoring documents:**
   - Identify the primary purpose of each document
   - Assign each type of information to the most appropriate document
   - Replace duplications with cross-references

5. **Red flags indicating redundancy:**
   - Copying lists of IDs between documents
   - Repeating detailed descriptions verbatim
   - Same table appearing in multiple places
   - Having to update information in more than one file

**Examples:**

✅ **Good separation:**
- requirements-by-version.md: Flat lookup table (ID → Version)
- versions-roadmap.md: Strategic narrative that references requirements-by-version.md

❌ **Bad redundancy:**
- Both documents contain identical lists of requirement IDs for each version

---
*Last updated: 2025-11-10*