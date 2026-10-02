---
name: add-broker-import
description: Add support for importing transaction history from a new broker (new TransactionHistoryParser adapter) or substantially rework an existing broker parser. Use when the user provides a new broker export file, asks to support a new broker, or reports that a broker's export format changed.
---

# Add a broker import adapter

Conventions: `.claude/rules/import.md`. Reference implementations, in increasing complexity:
- `infrastructure/external/degiro/DegiroCsvParser`
- `infrastructure/external/mbank/MBankCsvParser`
- `infrastructure/external/xtb/XtbXlsxParser` plus its helper classes

Port: `application/port/out/TransactionHistoryParser`. `InitiateImportUseCaseService` picks the parser by `brokerName()`, case-insensitively, so there's nothing to register by hand. Spring injects every parser bean.

## 1. Analyze real data first (mandatory)
- Get **every** sample export the user has: `.context/attachments/` or ask. Don't design anything from documentation or assumptions alone.
- Write `docs/import-formats/<broker>-format-analysis.md`, following `xtb-format-analysis.md`. Cover:
  - encoding, separators and number/date formats,
  - columns, and how the direction (buy/sell) is determined,
  - currency and quantity semantics,
  - how instruments are identified (name, ISIN, ticker),
  - rows to skip (fees, FX, dividends, corporate actions),
  - edge cases.
- Present the analysis and any open questions to the user **before** writing code.

## 2. Plan the conflicts with the user
Import replaces all positions of the target account. Raise these cases explicitly:
- the account was previously filled from another broker or by manual entry,
- an instrument isn't in the catalog,
- the file contains sells without matching buys (FIFO underflow),
- the file contains currencies the system doesn't support.

## 3. Catalog coverage
List every instrument in the samples that has an open position. For any missing from the catalog, run the `add-instruments` skill. Broker-specific names resolve through user-confirmed `BrokerInstrumentMapping`. Never auto-match them.

## 4. Implement (TDD)
1. Write a parser unit test **first**.
   - Fixtures must be anonymized: inline text blocks for CSV, or a workbook built in code for XLSX (see `XtbXlsxParserTest`). **Never commit real broker files.**
   - Cover: buy, sell, partial sell, skipped row types, the number format, encoding, and empty or invalid files.
2. Implement `infrastructure/external/<broker>/<Broker><Format>Parser` (`@Component`).
   - Return `ParseResult` with `RawTransaction`s and ticker hints where the file provides them.
   - Leave currency resolution to the catalog; don't trust the file.
3. Add Cucumber scenarios via the `bdd-scenario` skill, tagged `@import @<broker> @FR-021 @FR-032`.

## 5. Finish
- Run `./gradlew spotlessApply && ./gradlew clean build`.
- The frontend upload takes the broker as free text, so it needs no change. Run a manual check with `./dev.sh reset` and the real file.
- Update FR-021's "Broker Accounts" line in `requirements/functional/functional-requirements.md`.
- Update the broker list in the "Parsers" section of `.claude/rules/import.md`.
