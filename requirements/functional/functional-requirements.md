# Functional Requirements

**Project:** Investment Tracker
**Document Version:** 1.0
**Last Updated:** 2025-11-10
**Status:** Draft

---

## Document Overview

This document contains all functional requirements for the Investment Tracker application, extracted from Cucumber feature files and functional requirements interviews. Each requirement has a unique identifier (FR-XXX) for traceability.

## Requirements Organization

Requirements are organized into the following categories:
1. **Portfolio Management** - Viewing, aggregation, and metrics
2. **Position Management** - Individual position tracking and details
3. **Data Import** - CSV import and validation
4. **Manual Entry** - Position entry and editing
5. **Price Management** - Updates and history
6. **Reconciliation** - Broker statement verification
7. **Account Management** - Multiple accounts
8. **Metrics & Calculations** - P&L, XIRR, and financial calculations

## Cross-References

- **Scenario Mapping:** For detailed mapping between Cucumber scenarios and requirements, see `planning/scenarios-to-requirements.md`
- **Version Mapping:** For requirement implementation by version, see `planning/requirements-by-version.md`

---

## 1. Portfolio Management

### FR-001: View Portfolio Summary
**Category:** Portfolio Management
**Priority:** Must Have
**Description:** User can view aggregated portfolio summary showing total current value, total invested amount, total P&L (amount and percentage), and XIRR across all accounts.
**Related Scenarios:** View total portfolio value, Price update affects portfolio metrics
**Cucumber Tags:** @FR-001
**Dependencies:** FR-002, FR-031, FR-032
**Target Version:** v0.1 (without XIRR), v0.3 (with XIRR)

### FR-002: View Portfolio with Positive Returns
**Category:** Portfolio Management
**Priority:** Must Have
**Description:** System accurately calculates and displays portfolio performance when total returns are positive, showing correct current value, invested amount, P&L in PLN and percentage.
**Related Scenarios:** View portfolio with positive returns
**Cucumber Tags:** @FR-002
**Dependencies:** FR-031, FR-032
**Target Version:** v0.1

### FR-003: View Portfolio with Negative Returns
**Category:** Portfolio Management
**Priority:** Must Have
**Description:** System accurately calculates and displays portfolio performance when total returns are negative, showing correct current value, invested amount, negative P&L in PLN and percentage.
**Related Scenarios:** View portfolio with negative returns
**Cucumber Tags:** @FR-003
**Dependencies:** FR-031, FR-032
**Target Version:** v0.1

### FR-004: View Empty Portfolio
**Category:** Portfolio Management
**Priority:** Must Have
**Description:** When user has no positions, system displays zero values (0 PLN for current value and invested amount) and shows message "No positions found".
**Related Scenarios:** View empty portfolio
**Cucumber Tags:** @FR-004
**Dependencies:** None
**Target Version:** v0.1

### FR-005: Multi-Account Position Aggregation
**Category:** Portfolio Management
**Priority:** Must Have
**Description:** System aggregates positions of the same instrument across different broker accounts into a single view, calculating total quantity, weighted average cost basis, total current value, and total P&L.
**Related Scenarios:** View aggregated positions across accounts
- Import aggregation across accounts
**Cucumber Tags:** @FR-005
**Dependencies:** FR-021, FR-031, FR-032
**Target Version:** v0.2

### FR-006: Portfolio XIRR Calculation
**Category:** Portfolio Management
**Priority:** Must Have
**Description:** System calculates and displays portfolio-level XIRR (Extended Internal Rate of Return) considering investment dates and amounts across all positions.
**Related Scenarios:** View total portfolio value
**Cucumber Tags:** @FR-006
**Dependencies:** FR-031, FR-033
**Target Version:** v0.3

---

## 2. Position Management

### FR-011: View Individual Position Details
**Category:** Position Management
**Priority:** Must Have
**Description:** User can view detailed information for a specific position including quantity, average cost basis, invested amount, current value, P&L (amount and percentage), and position-level XIRR.
**Related Scenarios:** View individual stock position
**Cucumber Tags:** @FR-011
**Dependencies:** FR-031, FR-032
**Target Version:** v0.1 (without XIRR), v0.3 (with XIRR)

### FR-012: View ETF Position with Loss
**Category:** Position Management
**Priority:** Must Have
**Description:** System accurately displays ETF position details including loss scenarios with correct P&L calculations.
**Related Scenarios:** View ETF position with loss
**Cucumber Tags:** @FR-012
**Dependencies:** FR-031, FR-032
**Target Version:** v0.1

### FR-013: View Polish Government Bond Position
**Category:** Position Management
**Priority:** Should Have
**Description:** User can view Polish government bond positions showing invested amount and current value (bonds handled differently than stocks - no quantity/price split).
**Related Scenarios:** View Polish government bond position
**Cucumber Tags:** @FR-013
**Dependencies:** FR-031, FR-032
**Target Version:** v0.6

### FR-014: List All Positions
**Category:** Position Management
**Priority:** Must Have
**Description:** User can view a list of all positions sorted by current value (descending), with each position showing instrument name, current value, and P&L percentage.
**Related Scenarios:** List all positions
- Successful reconciliation
**Cucumber Tags:** @FR-014
**Dependencies:** FR-031
**Target Version:** v0.1

### FR-015: Position XIRR Calculation
**Category:** Position Management
**Priority:** Must Have
**Description:** System calculates and displays position-level XIRR considering transaction dates and amounts for that specific instrument.
**Related Scenarios:** View individual stock position
**Cucumber Tags:** @FR-015
**Dependencies:** FR-033
**Target Version:** v0.3

---

## 3. Data Import

### FR-021: Import Positions from Broker File
**Category:** Data Import
**Priority:** Must Have
**Description:** System can import position data from broker-specific CSV export files, creating positions in the system based on valid data.
**Related Scenarios:** Import positions from broker file
**Cucumber Tags:** @FR-021
**Dependencies:** None
**Target Version:** v0.2 (first broker), v0.5 (all 6 brokers)

### FR-022: Import Validation - Missing Instrument Identifier
**Category:** Data Import
**Priority:** Must Have
**Description:** System validates that every import row contains an instrument identifier and rejects import with clear error message if missing.
**Related Scenarios:** Import validation - missing instrument identifier
**Cucumber Tags:** @FR-022
**Dependencies:** FR-021
**Target Version:** v0.2

### FR-023: Import Validation - Missing Quantity
**Category:** Data Import
**Priority:** Must Have
**Description:** System validates that every import row contains a quantity value and rejects import with clear error message if missing.
**Related Scenarios:** Import validation - missing quantity
**Cucumber Tags:** @FR-023
**Dependencies:** FR-021
**Target Version:** v0.2

### FR-024: Import Validation - Missing Account Identifier
**Category:** Data Import
**Priority:** Must Have
**Description:** System validates that import file contains account information and rejects import with clear error message if missing.
**Related Scenarios:** Import validation - missing account identifier
**Cucumber Tags:** @FR-024
**Dependencies:** FR-021
**Target Version:** v0.2

### FR-025: Import Validation - Invalid Quantity (Negative)
**Category:** Data Import
**Priority:** Must Have
**Description:** System validates that quantity values are positive and rejects import with clear error message if negative.
**Related Scenarios:** Import validation - invalid quantity (negative)
**Cucumber Tags:** @FR-025
**Dependencies:** FR-021
**Target Version:** v0.2

### FR-026: Import Validation - Invalid Quantity (Zero)
**Category:** Data Import
**Priority:** Must Have
**Description:** System validates that quantity values are greater than zero and rejects import with clear error message if zero.
**Related Scenarios:** Import validation - invalid quantity (zero)
**Cucumber Tags:** @FR-026
**Dependencies:** FR-021
**Target Version:** v0.2

### FR-027: Import Validation - Invalid Quantity (Non-Numeric)
**Category:** Data Import
**Priority:** Must Have
**Description:** System validates that quantity values are numeric and rejects import with clear error message if non-numeric.
**Related Scenarios:** Import validation - invalid quantity (non-numeric)
**Cucumber Tags:** @FR-027
**Dependencies:** FR-021
**Target Version:** v0.2

### FR-028: Import Validation - Invalid Average Cost
**Category:** Data Import
**Priority:** Must Have
**Description:** System validates that average cost values are positive and rejects import with clear error message if negative.
**Related Scenarios:** Import validation - invalid average cost
**Cucumber Tags:** @FR-028
**Dependencies:** FR-021
**Target Version:** v0.2

### FR-029: Import with Missing Optional Fields
**Category:** Data Import
**Priority:** Should Have
**Description:** System allows import when optional fields (like average cost) are missing, creates positions without cost basis, and displays warning message about positions requiring manual cost entry.
**Related Scenarios:** Import with missing optional fields
**Cucumber Tags:** @FR-029
**Dependencies:** FR-021
**Target Version:** v0.2

### FR-030: Import Aggregation Across Accounts
**Category:** Data Import
**Priority:** Must Have
**Description:** When importing positions from multiple broker accounts for the same instrument, system aggregates them correctly in portfolio view.
**Related Scenarios:** Import aggregation across accounts
- View aggregated positions across accounts
**Cucumber Tags:** @FR-030
**Dependencies:** FR-021, FR-005
**Target Version:** v0.2

### FR-031: Import Duplicate Prevention
**Category:** Data Import
**Priority:** Should Have
**Description:** System detects when same file has already been imported and prevents duplicate position creation with appropriate warning message.
**Related Scenarios:** Import duplicate prevention
**Cucumber Tags:** @FR-031
**Dependencies:** FR-021
**Target Version:** v1.0

---

## 4. Manual Entry

### FR-041: Manual Entry of Stock Position
**Category:** Manual Entry
**Priority:** Must Have
**Description:** User can manually add a stock position by entering instrument name, quantity, average cost, and account, with system confirming successful creation.
**Related Scenarios:** Manual entry of stock position
**Cucumber Tags:** @FR-041
**Dependencies:** None
**Target Version:** v0.1

### FR-042: Manual Entry of ETF Position
**Category:** Manual Entry
**Priority:** Must Have
**Description:** User can manually add an ETF position by entering instrument name, quantity, average cost, and account, with system confirming successful creation.
**Related Scenarios:** Manual entry of ETF position
**Cucumber Tags:** @FR-042
**Dependencies:** None
**Target Version:** v0.1

### FR-043: Manual Entry of Polish Government Bonds
**Category:** Manual Entry
**Priority:** Should Have
**Description:** User can manually add Polish government bond position by entering bond series, invested amount, current value, and account (different from stock entry - no quantity/price).
**Related Scenarios:** Manual entry of Polish government bonds
- Polish government bond value update
**Cucumber Tags:** @FR-043
**Dependencies:** None
**Target Version:** v0.6

### FR-044: Manual Entry Validation - Missing Required Fields
**Category:** Manual Entry
**Priority:** Must Have
**Description:** System validates that required fields (instrument name) are provided and prevents position creation with clear error message if missing.
**Related Scenarios:** Manual entry validation - missing required fields
**Cucumber Tags:** @FR-044
**Dependencies:** FR-041
**Target Version:** v0.1

### FR-045: Manual Entry Validation - Invalid Quantity
**Category:** Manual Entry
**Priority:** Must Have
**Description:** System validates that quantity values are positive and prevents position creation with clear error message if negative or invalid.
**Related Scenarios:** Manual entry validation - invalid quantity
**Cucumber Tags:** @FR-045
**Dependencies:** FR-041
**Target Version:** v0.1

### FR-046: Manual Entry Validation - Invalid Average Cost
**Category:** Manual Entry
**Priority:** Must Have
**Description:** System validates that average cost values are greater than zero and prevents position creation with clear error message if zero or negative.
**Related Scenarios:** Manual entry validation - invalid average cost
**Cucumber Tags:** @FR-046
**Dependencies:** FR-041
**Target Version:** v0.1

---

## 5. Price Management

### FR-051: Fetch Prices from Yahoo Finance API
**Category:** Price Management
**Priority:** Must Have
**Description:** System fetches current market prices from Yahoo Finance API during position import and stores them in database, enabling portfolio valuation calculations.
**Related Scenarios:** Import positions with API price fetch
**Cucumber Tags:** @FR-051
**Dependencies:** FR-021
**Target Version:** v0.2

### FR-052: ~~Bulk Price Update from File~~ [DELETED]
**Status:** REMOVED - Manual price updates replaced by API-based approach

### FR-053: Price Changes Affect Portfolio Metrics
**Category:** Price Management
**Priority:** Must Have
**Description:** When prices are fetched from API and stored, system automatically recalculates affected position values and portfolio-level metrics (P&L, return percentage).
**Related Scenarios:** Import positions with API price fetch
**Cucumber Tags:** @FR-053
**Dependencies:** FR-051, FR-001
**Target Version:** v0.2

### FR-054: API Price Validation - Negative Price
**Category:** Price Management
**Priority:** Must Have
**Description:** System validates that prices fetched from API are positive and handles API errors gracefully if negative values are returned.
**Related Scenarios:** API price validation
**Cucumber Tags:** @FR-054
**Dependencies:** FR-051
**Target Version:** v0.2

### FR-055: API Price Validation - Zero Price
**Category:** Price Management
**Priority:** Must Have
**Description:** System validates that prices fetched from API are greater than zero and handles API errors gracefully if zero values are returned.
**Related Scenarios:** API price validation
**Cucumber Tags:** @FR-055
**Dependencies:** FR-051
**Target Version:** v0.2

### FR-056: Display Last Price Refresh Date
**Category:** Price Management
**Priority:** Must Have
**Description:** Portfolio view displays the date and time when prices were last fetched from API, allowing user to know data freshness.
**Related Scenarios:** Display last price refresh date in portfolio
**Cucumber Tags:** @FR-056
**Dependencies:** FR-051
**Target Version:** v0.2

### FR-057: ~~Polish Government Bond Value Update~~ [DELETED]
**Status:** REMOVED - Polish government bonds use constant pricing logic, no price updates needed

---

## 6. Reconciliation

### FR-061: Successful Reconciliation
**Category:** Reconciliation
**Priority:** Should Have
**Description:** System can compare system positions against broker statement data and confirm successful match when all positions have matching quantity and value.
**Related Scenarios:** Successful reconciliation
- Reconciliation with quantity mismatch
- Reconciliation with missing position in system
- Reconciliation with extra position in system
- Value reconciliation within tolerance
- Reconciliation history
**Cucumber Tags:** @FR-061
**Dependencies:** FR-014
**Target Version:** v0.7

### FR-062: Reconciliation with Quantity Mismatch
**Category:** Reconciliation
**Priority:** Should Have
**Description:** System detects and reports when position quantities differ between system and broker statement, showing specific mismatch details.
**Related Scenarios:** Reconciliation with quantity mismatch
**Cucumber Tags:** @FR-062
**Dependencies:** FR-061
**Target Version:** v0.7

### FR-063: Reconciliation with Missing Position in System
**Category:** Reconciliation
**Priority:** Should Have
**Description:** System detects and reports when broker statement contains positions not present in system.
**Related Scenarios:** Reconciliation with missing position in system
**Cucumber Tags:** @FR-063
**Dependencies:** FR-061
**Target Version:** v0.7

### FR-064: Reconciliation with Extra Position in System
**Category:** Reconciliation
**Priority:** Should Have
**Description:** System detects and reports when system contains positions not present in broker statement.
**Related Scenarios:** Reconciliation with extra position in system
**Cucumber Tags:** @FR-064
**Dependencies:** FR-061
**Target Version:** v0.7

### FR-065: Value Reconciliation within Tolerance
**Category:** Reconciliation
**Priority:** Should Have
**Description:** System allows configurable tolerance for value differences and considers reconciliation successful if values differ by less than tolerance percentage.
**Related Scenarios:** Value reconciliation within tolerance
**Cucumber Tags:** @FR-065
**Dependencies:** FR-061
**Target Version:** v0.7

### FR-066: Reconciliation History
**Category:** Reconciliation
**Priority:** Could Have
**Description:** User can view history of past reconciliations showing date, broker, status, positions checked, and issues found.
**Related Scenarios:** Reconciliation history
**Cucumber Tags:** @FR-066
**Dependencies:** FR-061
**Target Version:** v1.0

---

## 7. Account Management

### FR-071: Multiple Account Support
**Category:** Account Management
**Priority:** Must Have
**Description:** System supports tracking positions across multiple broker accounts (up to 10 accounts) with different types (regular brokers, IKE, IKZE, Polish government bonds account).
**Related Scenarios:** View aggregated positions across accounts
- Import aggregation across accounts
**Cucumber Tags:** @FR-071
**Dependencies:** None
**Target Version:** v0.2

### FR-072: Account-Level Position View
**Category:** Account Management
**Priority:** Should Have
**Description:** User can view positions filtered by specific account (in addition to aggregated view).
**Related Scenarios:**
- Not explicitly tested (implied by multi-account scenarios)
**Cucumber Tags:** @FR-072
**Dependencies:** FR-071
**Target Version:** v0.2

---

## 8. Metrics & Calculations

### FR-081: Current Value Calculation
**Category:** Metrics & Calculations
**Priority:** Must Have
**Description:** System calculates current value of position by multiplying quantity by current price (or using current value for bonds).
**Related Scenarios:** View total portfolio value
- View portfolio with positive returns
- View portfolio with negative returns
- View aggregated positions across accounts
- View individual stock position
- View ETF position with loss
- View Polish government bond position
- List all positions
- Manual price update for single instrument
- Bulk price update from file
- Price update affects portfolio metrics
- Polish government bond value update
**Cucumber Tags:** @FR-081
**Dependencies:** None
**Target Version:** v0.1

### FR-082: Invested Amount Calculation
**Category:** Metrics & Calculations
**Priority:** Must Have
**Description:** System calculates invested amount by multiplying quantity by average cost basis (or using invested amount for bonds).
**Related Scenarios:** View total portfolio value
- View portfolio with positive returns
- View portfolio with negative returns
- View aggregated positions across accounts
- View individual stock position
- View ETF position with loss
- View Polish government bond position
**Cucumber Tags:** @FR-082
**Dependencies:** None
**Target Version:** v0.1

### FR-083: P&L Calculation (Amount)
**Category:** Metrics & Calculations
**Priority:** Must Have
**Description:** System calculates profit/loss amount as difference between current value and invested amount, supporting both positive and negative values.
**Related Scenarios:** View total portfolio value
- View portfolio with positive returns
- View portfolio with negative returns
- View aggregated positions across accounts
- View individual stock position
- View ETF position with loss
- View Polish government bond position
- List all positions
- Price update affects portfolio metrics
- Polish government bond value update
**Cucumber Tags:** @FR-083
**Dependencies:** FR-081, FR-082
**Target Version:** v0.1

### FR-084: P&L Calculation (Percentage)
**Category:** Metrics & Calculations
**Priority:** Must Have
**Description:** System calculates profit/loss percentage as (P&L amount / invested amount) * 100, supporting both positive and negative percentages.
**Related Scenarios:** View total portfolio value
- View portfolio with positive returns
- View portfolio with negative returns
- View individual stock position
- View ETF position with loss
- View Polish government bond position
- List all positions
- Price update affects portfolio metrics
**Cucumber Tags:** @FR-084
**Dependencies:** FR-083
**Target Version:** v0.1

### FR-085: Average Cost Basis Calculation (Multi-Account)
**Category:** Metrics & Calculations
**Priority:** Must Have
**Description:** System calculates weighted average cost basis when same instrument exists in multiple accounts.
**Related Scenarios:** View aggregated positions across accounts
**Cucumber Tags:** @FR-085
**Dependencies:** FR-005
**Target Version:** v0.2

### FR-086: XIRR Calculation (Position-Level)
**Category:** Metrics & Calculations
**Priority:** Must Have
**Description:** System calculates Extended Internal Rate of Return (XIRR) for individual positions considering transaction dates and cash flows.
**Related Scenarios:** View individual stock position
**Cucumber Tags:** @FR-086
**Dependencies:** None
**Target Version:** v0.3

### FR-087: XIRR Calculation (Portfolio-Level)
**Category:** Metrics & Calculations
**Priority:** Must Have
**Description:** System calculates Extended Internal Rate of Return (XIRR) for entire portfolio considering all transactions and cash flows across all positions.
**Related Scenarios:** View total portfolio value
**Cucumber Tags:** @FR-087
**Dependencies:** FR-086
**Target Version:** v0.3

### FR-088: Average Cost Method for Partial Sales
**Category:** Metrics & Calculations
**Priority:** Could Have
**Description:** System uses average cost method when calculating cost basis for partial position sales (not MVP - only tracking current positions).
**Related Scenarios:**
- Not tested (future enhancement)
**Cucumber Tags:** @FR-088
**Dependencies:** None
**Target Version:** Future (v2.0+)

### FR-089: All Values Displayed in PLN
**Category:** Metrics & Calculations
**Priority:** Must Have
**Description:** System displays all monetary values in Polish Zloty (PLN) currency.
**Related Scenarios:** View total portfolio value
- View portfolio with positive returns
- View portfolio with negative returns
- View empty portfolio
- View individual stock position
- View ETF position with loss
- View Polish government bond position
- List all positions
**Cucumber Tags:** @FR-089
**Dependencies:** None
**Target Version:** v0.1

---

## 9. System-Level Requirements

### FR-091: Instrument Type Support
**Category:** System-Level
**Priority:** Must Have
**Description:** System supports three instrument types: stocks, ETFs (stock and bond ETFs), and Polish government bonds, with appropriate handling for each type.
**Related Scenarios:** View individual stock position
- View ETF position with loss
- View Polish government bond position
- Manual entry of stock position
- Manual entry of ETF position
- Manual entry of Polish government bonds
**Cucumber Tags:** @FR-091
**Dependencies:** None
**Target Version:** v0.1 (stocks/ETFs), v0.6 (bonds)

### FR-092: Current Position Tracking Only (MVP)
**Category:** System-Level
**Priority:** Must Have
**Description:** System tracks only currently owned positions (MVP scope). Historical positions and realized gains from sold positions are out of scope for initial versions.
**Related Scenarios:**
- Implicit system constraint (all scenarios work with current positions only)
**Cucumber Tags:** @FR-092
**Dependencies:** None
**Target Version:** v0.1

### FR-093: Aggregated View Across All Accounts
**Category:** System-Level
**Priority:** Must Have
**Description:** System's primary value is providing single aggregated view across all broker accounts. This is the main problem being solved.
**Related Scenarios:** View aggregated positions across accounts
- Import aggregation across accounts
**Cucumber Tags:** @FR-093
**Dependencies:** FR-071
**Target Version:** v0.2

### FR-094: No Dividend Tracking (MVP)
**Category:** System-Level
**Priority:** Must Have (as exclusion)
**Description:** System does not track dividends in MVP scope.
**Related Scenarios:**
- Explicit exclusion (no scenarios test dividend tracking)
**Cucumber Tags:** @FR-094
**Dependencies:** None
**Target Version:** v0.1 (excluded)

### FR-095: No Transaction Fees Tracking (MVP)
**Category:** System-Level
**Priority:** Must Have (as exclusion)
**Description:** System does not track transaction fees or commissions in MVP scope.
**Related Scenarios:**
- Explicit exclusion (no scenarios test fee tracking)
**Cucumber Tags:** @FR-095
**Dependencies:** None
**Target Version:** v0.1 (excluded)

### FR-096: No Corporate Actions Handling (MVP)
**Category:** System-Level
**Priority:** Must Have (as exclusion)
**Description:** System does not handle corporate actions (splits, mergers, spin-offs) in MVP scope.
**Related Scenarios:**
- Explicit exclusion (no scenarios test corporate actions)
**Cucumber Tags:** @FR-096
**Dependencies:** None
**Target Version:** v0.1 (excluded)

---

## Requirements Summary by Priority

### Must Have (MVP Critical)
- **Portfolio Management:** FR-001 to FR-006 (6 requirements)
- **Position Management:** FR-011, FR-012, FR-014, FR-015 (4 requirements)
- **Data Import:** FR-021 to FR-028, FR-030 (9 requirements)
- **Manual Entry:** FR-041, FR-042, FR-044 to FR-046 (5 requirements)
- **Price Management:** FR-051 to FR-055 (5 requirements)
- **Account Management:** FR-071 (1 requirement)
- **Metrics & Calculations:** FR-081 to FR-087, FR-089, FR-091 to FR-096 (13 requirements)

**Total Must Have:** 43 requirements

### Should Have (Important but can be deferred)
- **Position Management:** FR-013 (1 requirement)
- **Data Import:** FR-029, FR-031 (2 requirements)
- **Manual Entry:** FR-043 (1 requirement)
- **Price Management:** FR-056, FR-057 (2 requirements)
- **Reconciliation:** FR-061 to FR-065 (5 requirements)
- **Account Management:** FR-072 (1 requirement)

**Total Should Have:** 12 requirements

### Could Have (Nice to have)
- **Reconciliation:** FR-066 (1 requirement)
- **Metrics & Calculations:** FR-088 (1 requirement - future version)

**Total Could Have:** 2 requirements

**Grand Total:** 57 functional requirements

---

## Requirements Coverage by Version

### v0.1 (MVP - Domain Foundation)
**Requirements:** FR-001 to FR-004, FR-011, FR-012, FR-014, FR-041, FR-042, FR-044 to FR-046, FR-081 to FR-084, FR-089, FR-091 to FR-096
**Count:** 20 requirements (partial implementation for some)

### v0.2 (Multi-Account Aggregation)
**Requirements:** FR-005, FR-021 to FR-030, FR-071, FR-072, FR-085
**Count:** 13 new requirements

### v0.3 (Performance Tracking - XIRR)
**Requirements:** FR-006, FR-015, FR-086, FR-087
**Count:** 4 new requirements

### v0.4 (Price Management)
**Requirements:** FR-051 to FR-055
**Count:** 5 new requirements

### v0.5 (Complete Import Coverage)
**Requirements:** Extends FR-021 to all 6 broker formats
**Count:** 0 new requirements (extends existing)

### v0.6 (Polish Government Bonds)
**Requirements:** FR-013, FR-043, FR-057
**Count:** 3 new requirements

### v0.7 (Reconciliation)
**Requirements:** FR-061 to FR-065
**Count:** 5 new requirements

### v1.0 (Feature Complete)
**Requirements:** FR-031, FR-056, FR-066
**Count:** 3 new requirements (completes all MVP features)

### v2.0+ (Future)
**Requirements:** FR-088 and future enhancements
**Count:** TBD

---

## Traceability Matrix

### Requirements to Cucumber Scenarios

| Requirement ID | Feature File | Scenario(s) |
|----------------|--------------|-------------|
| FR-001 | portfolio-viewing.feature | View total portfolio value |
| FR-002 | portfolio-viewing.feature | View portfolio with positive returns |
| FR-003 | portfolio-viewing.feature | View portfolio with negative returns |
| FR-004 | portfolio-viewing.feature | View empty portfolio |
| FR-005 | portfolio-viewing.feature | View aggregated positions across accounts |
| FR-006 | portfolio-viewing.feature | View total portfolio value (XIRR) |
| FR-011 | position-details.feature | View individual stock position |
| FR-012 | position-details.feature | View ETF position with loss |
| FR-013 | position-details.feature | View Polish government bond position |
| FR-014 | position-details.feature | List all positions |
| FR-015 | position-details.feature | View individual stock position (XIRR) |
| FR-021 | data-import.feature | Import positions from broker file |
| FR-022 | data-import.feature | Import validation - missing instrument identifier |
| FR-023 | data-import.feature | Import validation - missing quantity |
| FR-024 | data-import.feature | Import validation - missing account identifier |
| FR-025 | data-import.feature | Import validation - invalid quantity (negative) |
| FR-026 | data-import.feature | Import validation - invalid quantity (zero) |
| FR-027 | data-import.feature | Import validation - invalid quantity (non-numeric) |
| FR-028 | data-import.feature | Import validation - invalid average cost |
| FR-029 | data-import.feature | Import with missing optional fields |
| FR-030 | data-import.feature | Import aggregation across accounts |
| FR-031 | data-import.feature | Import duplicate prevention |
| FR-041 | manual-entry.feature | Manual entry of stock position |
| FR-042 | manual-entry.feature | Manual entry of ETF position |
| FR-043 | manual-entry.feature | Manual entry of Polish government bonds |
| FR-044 | manual-entry.feature | Manual entry validation - missing required fields |
| FR-045 | manual-entry.feature | Manual entry validation - invalid quantity |
| FR-046 | manual-entry.feature | Manual entry validation - invalid average cost |
| FR-051 | price-updates.feature | Manual price update for single instrument |
| FR-052 | price-updates.feature | Bulk price update from file |
| FR-053 | price-updates.feature | Price update affects portfolio metrics |
| FR-054 | price-updates.feature | Price validation - negative price |
| FR-055 | price-updates.feature | Price validation - zero price |
| FR-056 | price-updates.feature | View price update history |
| FR-057 | price-updates.feature | Polish government bond value update |
| FR-061 | reconciliation.feature | Successful reconciliation |
| FR-062 | reconciliation.feature | Reconciliation with quantity mismatch |
| FR-063 | reconciliation.feature | Reconciliation with missing position in system |
| FR-064 | reconciliation.feature | Reconciliation with extra position in system |
| FR-065 | reconciliation.feature | Value reconciliation within tolerance |
| FR-066 | reconciliation.feature | Reconciliation history |

---

## Related Documents

- **Non-Functional Requirements:** /Users/michalrzodkiewicz/private/investments-tracker/requirements/non-functional/non-functional-requirements.md
- **Version Roadmap:** /Users/michalrzodkiewicz/private/investments-tracker/planning/VERSION-ROADMAP.md
- **Requirements Traceability Matrix:** /Users/michalrzodkiewicz/private/investments-tracker/planning/requirements-by-version.md
- **Domain Language:** /Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/ubiquitous-language.md
- **Cucumber Features:** /Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/*.feature

---

## Document Approval

**Status:** Draft
**Reviewed by:** Pending
**Approved by:** Pending
**Date:** 2025-11-10

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-10 | Claude | Initial version extracted from Cucumber features and interview |
