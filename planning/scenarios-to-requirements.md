# Feature to Requirements Mapping

**Document Version:** 1.0
**Last Updated:** 2025-11-10
**Purpose:** Bidirectional traceability between Cucumber scenarios and functional requirements

---

## portfolio-viewing.feature

### Scenario: "View total portfolio value"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/portfolio-viewing.feature:16`
**Requirements Covered:**
- FR-001: View Portfolio Summary
- FR-006: Portfolio XIRR Calculation
- FR-081: Current Value Calculation
- FR-082: Invested Amount Calculation
- FR-083: P&L Calculation (Amount)
- FR-084: P&L Calculation (Percentage)
- FR-087: XIRR Calculation (Portfolio-Level)
- FR-089: All Values Displayed in PLN

**Version:** v0.1 (partial - no XIRR), v0.3 (complete with XIRR)
**Categories:** Portfolio Management, Metrics

---

### Scenario: "View portfolio with positive returns"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/portfolio-viewing.feature:26`
**Requirements Covered:**
- FR-002: View Portfolio with Positive Returns
- FR-081: Current Value Calculation
- FR-082: Invested Amount Calculation
- FR-083: P&L Calculation (Amount)
- FR-084: P&L Calculation (Percentage)
- FR-089: All Values Displayed in PLN

**Version:** v0.1
**Categories:** Portfolio Management, Metrics

---

### Scenario: "View portfolio with negative returns"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/portfolio-viewing.feature:37`
**Requirements Covered:**
- FR-003: View Portfolio with Negative Returns
- FR-081: Current Value Calculation
- FR-082: Invested Amount Calculation
- FR-083: P&L Calculation (Amount)
- FR-084: P&L Calculation (Percentage)
- FR-089: All Values Displayed in PLN

**Version:** v0.1
**Categories:** Portfolio Management, Metrics

---

### Scenario: "View aggregated positions across accounts"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/portfolio-viewing.feature:48`
**Requirements Covered:**
- FR-005: Multi-Account Position Aggregation
- FR-030: Import Aggregation Across Accounts
- FR-071: Multiple Account Support
- FR-081: Current Value Calculation
- FR-082: Invested Amount Calculation
- FR-083: P&L Calculation (Amount)
- FR-085: Average Cost Basis Calculation (Multi-Account)
- FR-093: Aggregated View Across All Accounts

**Version:** v0.2
**Categories:** Portfolio Management, Multi-Account, Aggregation

---

### Scenario: "View empty portfolio"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/portfolio-viewing.feature:60`
**Requirements Covered:**
- FR-004: View Empty Portfolio
- FR-089: All Values Displayed in PLN

**Version:** v0.1
**Categories:** Portfolio Management

---

## position-details.feature

### Scenario: "View individual stock position"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/position-details.feature:16`
**Requirements Covered:**
- FR-011: View Individual Position Details
- FR-015: Position XIRR Calculation
- FR-081: Current Value Calculation
- FR-082: Invested Amount Calculation
- FR-083: P&L Calculation (Amount)
- FR-084: P&L Calculation (Percentage)
- FR-086: XIRR Calculation (Position-Level)
- FR-089: All Values Displayed in PLN
- FR-091: Instrument Type Support (stocks)

**Version:** v0.1 (partial - no XIRR), v0.3 (complete with XIRR)
**Categories:** Position Management, Metrics

---

### Scenario: "View ETF position with loss"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/position-details.feature:30`
**Requirements Covered:**
- FR-012: View ETF Position with Loss
- FR-081: Current Value Calculation
- FR-082: Invested Amount Calculation
- FR-083: P&L Calculation (Amount)
- FR-084: P&L Calculation (Percentage)
- FR-089: All Values Displayed in PLN
- FR-091: Instrument Type Support (ETFs)

**Version:** v0.1
**Categories:** Position Management, Metrics

---

### Scenario: "View Polish government bond position"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/position-details.feature:43`
**Requirements Covered:**
- FR-013: View Polish Government Bond Position
- FR-081: Current Value Calculation
- FR-082: Invested Amount Calculation
- FR-083: P&L Calculation (Amount)
- FR-084: P&L Calculation (Percentage)
- FR-089: All Values Displayed in PLN
- FR-091: Instrument Type Support (bonds)

**Version:** v0.6
**Categories:** Position Management, Bonds

---

### Scenario: "List all positions"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/position-details.feature:54`
**Requirements Covered:**
- FR-014: List All Positions
- FR-081: Current Value Calculation
- FR-083: P&L Calculation (Amount)
- FR-084: P&L Calculation (Percentage)
- FR-089: All Values Displayed in PLN

**Version:** v0.1
**Categories:** Position Management

---

## data-import.feature

### Scenario: "Import positions from broker file"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/data-import.feature:21`
**Requirements Covered:**
- FR-021: Import Positions from Broker File

**Version:** v0.2 (first broker), v0.5 (all brokers)
**Categories:** Data Import

---

### Scenario: "Import validation - missing instrument identifier"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/data-import.feature:34`
**Requirements Covered:**
- FR-022: Import Validation - Missing Instrument Identifier

**Version:** v0.2
**Categories:** Data Import, Validation

---

### Scenario: "Import validation - missing quantity"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/data-import.feature:42`
**Requirements Covered:**
- FR-023: Import Validation - Missing Quantity

**Version:** v0.2
**Categories:** Data Import, Validation

---

### Scenario: "Import validation - missing account identifier"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/data-import.feature:50`
**Requirements Covered:**
- FR-024: Import Validation - Missing Account Identifier

**Version:** v0.2
**Categories:** Data Import, Validation

---

### Scenario: "Import validation - invalid quantity (negative)"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/data-import.feature:58`
**Requirements Covered:**
- FR-025: Import Validation - Invalid Quantity (Negative)

**Version:** v0.2
**Categories:** Data Import, Validation

---

### Scenario: "Import validation - invalid quantity (zero)"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/data-import.feature:66`
**Requirements Covered:**
- FR-026: Import Validation - Invalid Quantity (Zero)

**Version:** v0.2
**Categories:** Data Import, Validation

---

### Scenario: "Import validation - invalid quantity (non-numeric)"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/data-import.feature:74`
**Requirements Covered:**
- FR-027: Import Validation - Invalid Quantity (Non-Numeric)

**Version:** v0.2
**Categories:** Data Import, Validation

---

### Scenario: "Import validation - invalid average cost"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/data-import.feature:82`
**Requirements Covered:**
- FR-028: Import Validation - Invalid Average Cost

**Version:** v0.2
**Categories:** Data Import, Validation

---

### Scenario: "Import with missing optional fields"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/data-import.feature:90`
**Requirements Covered:**
- FR-029: Import with Missing Optional Fields

**Version:** v0.2
**Categories:** Data Import, Validation

---

### Scenario: "Import aggregation across accounts"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/data-import.feature:102`
**Requirements Covered:**
- FR-030: Import Aggregation Across Accounts
- FR-005: Multi-Account Position Aggregation
- FR-071: Multiple Account Support
- FR-093: Aggregated View Across All Accounts

**Version:** v0.2
**Categories:** Data Import, Multi-Account, Aggregation

---

### Scenario: "Import duplicate prevention"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/data-import.feature:110`
**Requirements Covered:**
- FR-031: Import Duplicate Prevention

**Version:** v1.0
**Categories:** Data Import, Validation

---

## manual-entry.feature

### Scenario: "Manual entry of stock position"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/manual-entry.feature:11`
**Requirements Covered:**
- FR-041: Manual Entry of Stock Position
- FR-091: Instrument Type Support (stocks)

**Version:** v0.1
**Categories:** Manual Entry

---

### Scenario: "Manual entry of ETF position"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/manual-entry.feature:25`
**Requirements Covered:**
- FR-042: Manual Entry of ETF Position
- FR-091: Instrument Type Support (ETFs)

**Version:** v0.1
**Categories:** Manual Entry

---

### Scenario: "Manual entry of Polish government bonds"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/manual-entry.feature:38`
**Requirements Covered:**
- FR-043: Manual Entry of Polish Government Bonds
- FR-091: Instrument Type Support (bonds)

**Version:** v0.6
**Categories:** Manual Entry, Bonds

---

### Scenario: "Manual entry validation - missing required fields"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/manual-entry.feature:53`
**Requirements Covered:**
- FR-044: Manual Entry Validation - Missing Required Fields

**Version:** v0.1
**Categories:** Manual Entry, Validation

---

### Scenario: "Manual entry validation - invalid quantity"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/manual-entry.feature:61`
**Requirements Covered:**
- FR-045: Manual Entry Validation - Invalid Quantity

**Version:** v0.1
**Categories:** Manual Entry, Validation

---

### Scenario: "Manual entry validation - invalid average cost"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/manual-entry.feature:69`
**Requirements Covered:**
- FR-046: Manual Entry Validation - Invalid Average Cost

**Version:** v0.1
**Categories:** Manual Entry, Validation

---

## price-updates.feature

### Scenario: "Manual price update for single instrument"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/price-updates.feature:11`
**Requirements Covered:**
- FR-051: Manual Price Update for Single Instrument
- FR-053: Price Update Affects Portfolio Metrics
- FR-081: Current Value Calculation

**Version:** v0.4
**Categories:** Price Management

---

### Scenario: "Bulk price update from file"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/price-updates.feature:21`
**Requirements Covered:**
- FR-052: Bulk Price Update from File
- FR-053: Price Update Affects Portfolio Metrics
- FR-081: Current Value Calculation

**Version:** v0.4
**Categories:** Price Management

---

### Scenario: "Price update affects portfolio metrics"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/price-updates.feature:34`
**Requirements Covered:**
- FR-053: Price Update Affects Portfolio Metrics
- FR-001: View Portfolio Summary
- FR-081: Current Value Calculation
- FR-083: P&L Calculation (Amount)
- FR-084: P&L Calculation (Percentage)

**Version:** v0.4
**Categories:** Price Management, Portfolio, Metrics

---

### Scenario: "Price validation - negative price"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/price-updates.feature:44`
**Requirements Covered:**
- FR-054: Price Validation - Negative Price

**Version:** v0.4
**Categories:** Price Management, Validation

---

### Scenario: "Price validation - zero price"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/price-updates.feature:52`
**Requirements Covered:**
- FR-055: Price Validation - Zero Price

**Version:** v0.4
**Categories:** Price Management, Validation

---

### Scenario: "View price update history"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/price-updates.feature:60`
**Requirements Covered:**
- FR-056: View Price Update History

**Version:** v1.0
**Categories:** Price Management, History

---

### Scenario: "Polish government bond value update"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/price-updates.feature:70`
**Requirements Covered:**
- FR-057: Polish Government Bond Value Update
- FR-043: Manual Entry of Polish Government Bonds
- FR-081: Current Value Calculation
- FR-083: P&L Calculation (Amount)

**Version:** v0.6
**Categories:** Price Management, Bonds

---

## reconciliation.feature

### Scenario: "Successful reconciliation"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/reconciliation.feature:11`
**Requirements Covered:**
- FR-061: Successful Reconciliation
- FR-014: List All Positions

**Version:** v0.7
**Categories:** Reconciliation

---

### Scenario: "Reconciliation with quantity mismatch"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/reconciliation.feature:26`
**Requirements Covered:**
- FR-062: Reconciliation with Quantity Mismatch
- FR-061: Successful Reconciliation

**Version:** v0.7
**Categories:** Reconciliation

---

### Scenario: "Reconciliation with missing position in system"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/reconciliation.feature:36`
**Requirements Covered:**
- FR-063: Reconciliation with Missing Position in System
- FR-061: Successful Reconciliation

**Version:** v0.7
**Categories:** Reconciliation

---

### Scenario: "Reconciliation with extra position in system"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/reconciliation.feature:45`
**Requirements Covered:**
- FR-064: Reconciliation with Extra Position in System
- FR-061: Successful Reconciliation

**Version:** v0.7
**Categories:** Reconciliation

---

### Scenario: "Value reconciliation within tolerance"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/reconciliation.feature:54`
**Requirements Covered:**
- FR-065: Value Reconciliation within Tolerance
- FR-061: Successful Reconciliation

**Version:** v0.7
**Categories:** Reconciliation

---

### Scenario: "Reconciliation history"
**File:** `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/reconciliation.feature:65`
**Requirements Covered:**
- FR-066: Reconciliation History
- FR-061: Successful Reconciliation

**Version:** v1.0
**Categories:** Reconciliation, History

---

## Summary Statistics

### Scenarios by Feature File
- **portfolio-viewing.feature:** 5 scenarios
- **position-details.feature:** 4 scenarios
- **data-import.feature:** 11 scenarios
- **manual-entry.feature:** 6 scenarios
- **price-updates.feature:** 7 scenarios
- **reconciliation.feature:** 6 scenarios

**Total Scenarios:** 39

---

### Scenarios by Version

**v0.1:** 13 scenarios
- View portfolio with positive returns
- View portfolio with negative returns
- View empty portfolio
- View individual stock position (partial)
- View ETF position with loss
- List all positions
- Manual entry of stock position
- Manual entry of ETF position
- Manual entry validation - missing required fields
- Manual entry validation - invalid quantity
- Manual entry validation - invalid average cost
- View total portfolio value (partial)

**v0.2:** 11 scenarios
- View aggregated positions across accounts
- Import positions from broker file
- Import validation - missing instrument identifier
- Import validation - missing quantity
- Import validation - missing account identifier
- Import validation - invalid quantity (negative)
- Import validation - invalid quantity (zero)
- Import validation - invalid quantity (non-numeric)
- Import validation - invalid average cost
- Import with missing optional fields
- Import aggregation across accounts

**v0.3:** 2 scenarios (complete)
- View total portfolio value (complete with XIRR)
- View individual stock position (complete with XIRR)

**v0.4:** 5 scenarios
- Manual price update for single instrument
- Bulk price update from file
- Price update affects portfolio metrics
- Price validation - negative price
- Price validation - zero price

**v0.5:** 1 scenario (extension)
- Import positions from broker file (extended to all 6 brokers)

**v0.6:** 3 scenarios
- View Polish government bond position
- Manual entry of Polish government bonds
- Polish government bond value update

**v0.7:** 5 scenarios
- Successful reconciliation
- Reconciliation with quantity mismatch
- Reconciliation with missing position in system
- Reconciliation with extra position in system
- Value reconciliation within tolerance

**v1.0:** 2 scenarios
- Import duplicate prevention
- View price update history
- Reconciliation history

---

### Requirements Coverage Analysis

**Total Functional Requirements:** 57

**Requirements with Scenario Coverage:** 46 (81%)

**Requirements without Direct Scenario Coverage:** 11
- FR-072: Account-Level Position View (implied by FR-071)
- FR-088: Average Cost Method for Partial Sales (future)
- FR-092: Current Position Tracking Only (system-level constraint)
- FR-094: No Dividend Tracking (exclusion)
- FR-095: No Transaction Fees Tracking (exclusion)
- FR-096: No Corporate Actions Handling (exclusion)

Note: System-level requirements (FR-092, FR-094, FR-095, FR-096) are constraints rather than testable features. FR-072 is covered implicitly through multi-account scenarios.

---

### Scenarios Covering Multiple Requirements

**High Integration Scenarios (5+ requirements):**
1. "View total portfolio value" - 8 requirements
2. "View individual stock position" - 9 requirements
3. "View aggregated positions across accounts" - 8 requirements

**Medium Integration Scenarios (3-4 requirements):**
1. "View portfolio with positive returns" - 5 requirements
2. "View portfolio with negative returns" - 5 requirements
3. "View ETF position with loss" - 6 requirements
4. "View Polish government bond position" - 6 requirements
5. "Import aggregation across accounts" - 4 requirements
6. "Price update affects portfolio metrics" - 5 requirements
7. "Polish government bond value update" - 4 requirements

---

## Related Documents
- Functional Requirements: `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/functional-requirements.md`
- Version Roadmap: `/Users/michalrzodkiewicz/private/investments-tracker/planning/VERSION-ROADMAP.md`
- Ubiquitous Language: `/Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/ubiquitous-language.md`

---

**Document Status:** Complete
**Last Reviewed:** 2025-11-10
