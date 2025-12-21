# Requirements Traceability Matrix

**Project:** Investment Tracker
**Document Version:** 1.0
**Last Updated:** 2025-11-10
**Status:** Draft

---

## Document Purpose

This Requirements Traceability Matrix (RTM) provides complete mapping of all functional and non-functional requirements to their target versions, priorities, and categories. It enables tracking requirement implementation status and ensures complete coverage.

---

## Functional Requirements Traceability

| ID | Title | Category | Priority | Version | Dependencies |
|----|-------|----------|----------|---------|--------------|
| **FR-001** | View Portfolio Summary | Portfolio Management | Must Have | v0.1 (partial), v0.3 (complete) | FR-002, FR-031, FR-032 |
| **FR-002** | View Portfolio with Positive Returns | Portfolio Management | Must Have | v0.1 | FR-031, FR-032 |
| **FR-003** | View Portfolio with Negative Returns | Portfolio Management | Must Have | v0.1 | FR-031, FR-032 |
| **FR-004** | View Empty Portfolio | Portfolio Management | Must Have | v0.1 | None |
| **FR-005** | Multi-Account Position Aggregation | Portfolio Management | Must Have | v0.2 | FR-021, FR-031, FR-032 |
| **FR-006** | Portfolio XIRR Calculation | Portfolio Management | Must Have | v0.3 | FR-031, FR-033 |
| **FR-011** | View Individual Position Details | Position Management | Must Have | v0.1 (partial), v0.3 (complete) | FR-031, FR-032 |
| **FR-012** | View ETF Position with Loss | Position Management | Must Have | v0.1 | FR-031, FR-032 |
| **FR-013** | View Polish Government Bond Position | Position Management | Should Have | v0.6 | FR-031, FR-032 |
| **FR-014** | List All Positions | Position Management | Must Have | v0.1 | FR-031 |
| **FR-015** | Position XIRR Calculation | Position Management | Must Have | v0.3 | FR-033 |
| **FR-021** | Import Positions from Broker File | Data Import | Must Have | v0.2 (first), v0.5 (all 6) | None |
| **FR-022** | Import Validation - Missing Instrument | Data Import | Must Have | v0.2 | FR-021 |
| **FR-023** | Import Validation - Missing Quantity | Data Import | Must Have | v0.2 | FR-021 |
| **FR-024** | Import Validation - Missing Account | Data Import | Must Have | v0.2 | FR-021 |
| **FR-025** | Import Validation - Invalid Quantity (Negative) | Data Import | Must Have | v0.2 | FR-021 |
| **FR-026** | Import Validation - Invalid Quantity (Zero) | Data Import | Must Have | v0.2 | FR-021 |
| **FR-027** | Import Validation - Invalid Quantity (Non-Numeric) | Data Import | Must Have | v0.2 | FR-021 |
| **FR-028** | Import Validation - Invalid Average Cost | Data Import | Must Have | v0.2 | FR-021 |
| **FR-029** | Import with Missing Optional Fields | Data Import | Should Have | v0.2 | FR-021 |
| **FR-030** | Import Aggregation Across Accounts | Data Import | Must Have | v0.2 | FR-021, FR-005 |
| **FR-031** | Import Duplicate Prevention | Data Import | Should Have | v1.0 | FR-021 |
| **FR-041** | Manual Entry of Stock Position | Manual Entry | Must Have | v0.1 | None |
| **FR-042** | Manual Entry of ETF Position | Manual Entry | Must Have | v0.1 | None |
| **FR-043** | Manual Entry of Polish Government Bonds | Manual Entry | Should Have | v0.6 | None |
| **FR-044** | Manual Entry Validation - Missing Fields | Manual Entry | Must Have | v0.1 | FR-041 |
| **FR-045** | Manual Entry Validation - Invalid Quantity | Manual Entry | Must Have | v0.1 | FR-041 |
| **FR-046** | Manual Entry Validation - Invalid Cost | Manual Entry | Must Have | v0.1 | FR-041 |
| **FR-051** | Manual Price Update for Single Instrument | Price Management | Must Have | v0.4 | FR-011 |
| **FR-052** | Bulk Price Update from File | Price Management | Must Have | v0.4 | FR-051 |
| **FR-053** | Price Update Affects Portfolio Metrics | Price Management | Must Have | v0.4 | FR-051, FR-001 |
| **FR-054** | Price Validation - Negative Price | Price Management | Must Have | v0.4 | FR-051 |
| **FR-055** | Price Validation - Zero Price | Price Management | Must Have | v0.4 | FR-051 |
| **FR-056** | View Price Update History | Price Management | Should Have | v1.0 | FR-051 |
| **FR-057** | Polish Government Bond Value Update | Price Management | Should Have | v0.6 | FR-043 |
| **FR-061** | Successful Reconciliation | Reconciliation | Should Have | v0.7 | FR-014 |
| **FR-062** | Reconciliation with Quantity Mismatch | Reconciliation | Should Have | v0.7 | FR-061 |
| **FR-063** | Reconciliation with Missing Position | Reconciliation | Should Have | v0.7 | FR-061 |
| **FR-064** | Reconciliation with Extra Position | Reconciliation | Should Have | v0.7 | FR-061 |
| **FR-065** | Value Reconciliation within Tolerance | Reconciliation | Should Have | v0.7 | FR-061 |
| **FR-066** | Reconciliation History | Reconciliation | Could Have | v1.0 | FR-061 |
| **FR-071** | Multiple Account Support | Account Management | Must Have | v0.2 | None |
| **FR-072** | Account-Level Position View | Account Management | Should Have | v0.2 | FR-071 |
| **FR-081** | Current Value Calculation | Metrics & Calculations | Must Have | v0.1 | None |
| **FR-082** | Invested Amount Calculation | Metrics & Calculations | Must Have | v0.1 | None |
| **FR-083** | P&L Calculation (Amount) | Metrics & Calculations | Must Have | v0.1 | FR-081, FR-082 |
| **FR-084** | P&L Calculation (Percentage) | Metrics & Calculations | Must Have | v0.1 | FR-083 |
| **FR-085** | Average Cost Basis (Multi-Account) | Metrics & Calculations | Must Have | v0.2 | FR-005 |
| **FR-086** | XIRR Calculation (Position-Level) | Metrics & Calculations | Must Have | v0.3 | None |
| **FR-087** | XIRR Calculation (Portfolio-Level) | Metrics & Calculations | Must Have | v0.3 | FR-086 |
| **FR-088** | Average Cost Method for Sales | Metrics & Calculations | Could Have | v2.0+ | None |
| **FR-089** | All Values in PLN | Metrics & Calculations | Must Have | v0.1 | None |
| **FR-091** | Instrument Type Support | System-Level | Must Have | v0.1 (stocks/ETFs), v0.6 (bonds) | None |
| **FR-092** | Current Position Tracking Only | System-Level | Must Have | v0.1 | None |
| **FR-093** | Aggregated View Across All Accounts | System-Level | Must Have | v0.2 | FR-071 |
| **FR-094** | No Dividend Tracking (exclusion) | System-Level | Must Have | v0.1 | None |
| **FR-095** | No Transaction Fees (exclusion) | System-Level | Must Have | v0.1 | None |
| **FR-096** | No Corporate Actions (exclusion) | System-Level | Must Have | v0.1 | None |

**Total Functional Requirements:** 57

---

## Non-Functional Requirements Traceability

### Deployment & Infrastructure (NFR-001 to NFR-010)

| ID | Title | Category | Priority | Version |
|----|-------|----------|----------|---------|
| **NFR-001** | Web Application Deployment | Deployment & Infrastructure | Must Have | v0.1 |
| **NFR-002** | Cloud-Ready Architecture | Deployment & Infrastructure | Must Have | v0.1 (design), v2.0 (impl) |
| **NFR-003** | Modern Deployment Practices | Deployment & Infrastructure | Must Have | v2.0 |
| **NFR-004** | Local Deployment Support | Deployment & Infrastructure | Must Have | v0.1 |
| **NFR-005** | No Authentication for Local | Deployment & Infrastructure | Must Have | v0.1 |
| **NFR-006** | Cloud Deployment Support | Deployment & Infrastructure | Should Have | v2.0 |
| **NFR-007** | Cloud Authentication | Deployment & Infrastructure | Should Have | v2.0 |
| **NFR-008** | Encrypted Cloud Connections | Deployment & Infrastructure | Should Have | v2.0 |
| **NFR-009** | Cloud Database | Deployment & Infrastructure | Should Have | v2.0 |
| **NFR-010** | Browser Compatibility | Deployment & Infrastructure | Must Have | v0.1 |

### Performance & Scalability (NFR-011 to NFR-016)

| ID | Title | Category | Priority | Version |
|----|-------|----------|----------|---------|
| **NFR-011** | Availability for Personal Use | Performance & Scalability | Could Have | v0.1 |
| **NFR-012** | Data Volume Scalability | Performance & Scalability | Must Have | v1.0 (tested at scale) |
| **NFR-013** | Single User System | Performance & Scalability | Must Have | v0.1 |
| **NFR-014** | Reasonable Performance | Performance & Scalability | Should Have | v0.1 |
| **NFR-015** | Daily Price Updates | Performance & Scalability | Must Have | v0.4 (manual), v2.1 (auto) |
| **NFR-016** | Architecture for Growth | Performance & Scalability | Must Have | v0.1 (design), v1.0 (validation) |

### Security (NFR-021 to NFR-028)

| ID | Title | Category | Priority | Version |
|----|-------|----------|----------|---------|
| **NFR-021** | No Local Authentication | Security | Must Have | v0.1 |
| **NFR-022** | Cloud Authentication Required | Security | Should Have | v2.0 |
| **NFR-023** | Single User Access | Security | Must Have | v0.1 |
| **NFR-024** | Data Sensitivity Classification | Security | Must Have | v0.1 |
| **NFR-025** | Local HTTP Acceptable | Security | Must Have | v0.1 |
| **NFR-026** | Cloud HTTPS Mandatory | Security | Should Have | v2.0 |
| **NFR-027** | Data Encryption at Rest | Security | Must Have | v0.1 |
| **NFR-028** | Complete Audit Trail | Security | Must Have | v0.1 |

### Data Management (NFR-031 to NFR-037)

| ID | Title | Category | Priority | Version |
|----|-------|----------|----------|---------|
| **NFR-031** | Zero Data Loss Tolerance | Data Management | Must Have | v0.1 |
| **NFR-032** | Indefinite Data Retention | Data Management | Must Have | v0.1 |
| **NFR-033** | Manual Backup for Local | Data Management | Should Have | v0.1 |
| **NFR-034** | Automated Cloud Backups | Data Management | Should Have | v2.0 |
| **NFR-035** | ACID Compliance | Data Management | Must Have | v0.1 |
| **NFR-036** | PostgreSQL Database | Data Management | Must Have | v0.1 |
| **NFR-037** | Database Transaction Support | Data Management | Must Have | v0.1 |

### Quality Attributes (NFR-041 to NFR-048, NFR-071 to NFR-082, NFR-091 to NFR-092)

| ID | Title | Category | Priority | Version |
|----|-------|----------|----------|---------|
| **NFR-041** | Angular Frontend | Quality Attributes | Must Have | v0.1 |
| **NFR-042** | Modern Simple UI | Quality Attributes | Must Have | v0.1 |
| **NFR-043** | No Mobile Responsiveness Required | Quality Attributes | Could Have | Future |
| **NFR-044** | No Accessibility Requirements | Quality Attributes | Could Have | Future |
| **NFR-045** | Developer-Intuitive Interface | Quality Attributes | Must Have | v0.1 |
| **NFR-046** | CSV Import Support | Quality Attributes | Must Have | v0.2 (first), v0.5 (all 6) |
| **NFR-047** | No Export Required for MVP | Quality Attributes | Could Have | Future |
| **NFR-048** | Extensible Architecture | Quality Attributes | Must Have | v0.1 |
| **NFR-071** | Clean Code Quality | Quality Attributes | Must Have (5/5) | v0.1 |
| **NFR-072** | Comprehensive Test Coverage | Quality Attributes | Must Have (5/5) | v0.1 (70%+), v1.0 (85%+) |
| **NFR-073** | Complete Documentation | Quality Attributes | Must Have (5/5) | v0.1 |
| **NFR-074** | Quality Over Speed | Quality Attributes | Must Have (3/5) | v0.1 |
| **NFR-081** | No Privacy Compliance Required | Quality Attributes | Must Have | v0.1 |
| **NFR-082** | No Data Residency Restrictions | Quality Attributes | Must Have | v0.1 |
| **NFR-091** | Zero Infrastructure Budget | Quality Attributes | Must Have | v0.1 (local), v2.0 (cloud) |
| **NFR-092** | Zero Third-Party Service Budget | Quality Attributes | Must Have | v0.1 |

### Observability (NFR-051 to NFR-054)

| ID | Title | Category | Priority | Version |
|----|-------|----------|----------|---------|
| **NFR-051** | OTLP Observability | Observability | Must Have | v0.1 |
| **NFR-052** | Detailed Logging | Observability | Must Have | v0.1 |
| **NFR-053** | User-Friendly Error Messages | Observability | Must Have | v0.1 |
| **NFR-054** | Hexagonal Architecture Tracing | Observability | Must Have | v0.1 |

### Technology Stack (NFR-061 to NFR-068)

| ID | Title | Category | Priority | Version |
|----|-------|----------|----------|---------|
| **NFR-061** | Java Backend | Technology Stack | Must Have | v0.1 |
| **NFR-062** | Minimal Spring Boot Usage | Technology Stack | Must Have | v0.1 |
| **NFR-063** | Hexagonal Architecture | Technology Stack | Must Have | v0.1 |
| **NFR-064** | Framework-Independent Domain | Technology Stack | Must Have | v0.1 |
| **NFR-065** | Angular Frontend | Technology Stack | Must Have | v0.1 |
| **NFR-066** | Docker Containerization | Technology Stack | Must Have | v0.1 |
| **NFR-067** | Terraform Infrastructure as Code | Technology Stack | Should Have | v2.0 |
| **NFR-068** | Kubernetes Orchestration | Technology Stack | Should Have | v2.0 |

**Total Non-Functional Requirements:** 48

---

## Version Roadmap Mapping

### v0.1 - MVP (Domain Foundation)

**Functional Requirements (20):**
- FR-001 (partial), FR-002, FR-003, FR-004
- FR-011 (partial), FR-012, FR-014
- FR-041, FR-042, FR-044, FR-045, FR-046
- FR-081, FR-082, FR-083, FR-084, FR-089
- FR-091 (partial), FR-092, FR-094, FR-095, FR-096

**Non-Functional Requirements (37):**
- NFR-001, NFR-002, NFR-004, NFR-005, NFR-010
- NFR-011, NFR-013, NFR-014, NFR-016 (design)
- NFR-021, NFR-023, NFR-024, NFR-025, NFR-027, NFR-028
- NFR-031, NFR-032, NFR-033, NFR-035, NFR-036, NFR-037
- NFR-041, NFR-042, NFR-045, NFR-048
- NFR-051, NFR-052, NFR-053, NFR-054
- NFR-061, NFR-062, NFR-063, NFR-064, NFR-065, NFR-066
- NFR-071, NFR-072, NFR-073, NFR-074
- NFR-081, NFR-082, NFR-091, NFR-092

### v0.2 - Multi-Account Aggregation

**Functional Requirements (13):**
- FR-005, FR-021, FR-022, FR-023, FR-024, FR-025, FR-026, FR-027, FR-028, FR-029, FR-030
- FR-071, FR-072, FR-085, FR-093

**Non-Functional Requirements (1):**
- NFR-046 (first broker format)

### v0.3 - Performance Tracking (XIRR)

**Functional Requirements (4):**
- FR-001 (complete), FR-006, FR-011 (complete), FR-015
- FR-086, FR-087

**Non-Functional Requirements:** None (completes FR requirements)

### v0.4 - Price Management

**Functional Requirements (5):**
- FR-051, FR-052, FR-053, FR-054, FR-055

**Non-Functional Requirements:**
- NFR-015 (manual price updates)

### v0.5 - Complete Import Coverage

**Functional Requirements:**
- FR-021 (extends to all 6 broker formats)

**Non-Functional Requirements:**
- NFR-046 (all 6 broker formats)

### v0.6 - Polish Government Bonds

**Functional Requirements (3):**
- FR-013, FR-043, FR-057
- FR-091 (complete with bonds)

**Non-Functional Requirements:** None

### v0.7 - Reconciliation

**Functional Requirements (5):**
- FR-061, FR-062, FR-063, FR-064, FR-065

**Non-Functional Requirements:** None

### v1.0 - Feature Complete Local Version

**Functional Requirements (3):**
- FR-031, FR-056, FR-066

**Non-Functional Requirements (2):**
- NFR-012 (tested at scale)
- NFR-016 (validation)
- NFR-072 (85%+ coverage target)

### v2.0 - Cloud Deployment

**Functional Requirements:** None (same as v1.0)

**Non-Functional Requirements (8):**
- NFR-002 (implementation), NFR-003, NFR-006, NFR-007, NFR-008, NFR-009
- NFR-022, NFR-026, NFR-034
- NFR-067, NFR-068
- NFR-091 (cloud budget)

### v2.1+ - Future Enhancements

**Functional Requirements:**
- FR-088 (partial sales tracking)
- Future requirements TBD

**Non-Functional Requirements:**
- NFR-015 (automated price updates)
- NFR-043 (mobile responsiveness - optional)
- NFR-044 (accessibility - optional)
- NFR-047 (export capabilities - optional)

---

## Requirements by Priority

### Must Have (Critical Path)

**Functional Requirements (43):**
- Portfolio: FR-001 to FR-006
- Position: FR-011, FR-012, FR-014, FR-015
- Import: FR-021 to FR-028, FR-030
- Manual Entry: FR-041, FR-042, FR-044 to FR-046
- Price: FR-051 to FR-055
- Account: FR-071
- Metrics: FR-081 to FR-087, FR-089, FR-091 to FR-096

**Non-Functional Requirements (37):**
- Deployment: NFR-001, NFR-002, NFR-004, NFR-005, NFR-010
- Performance: NFR-012, NFR-013, NFR-015, NFR-016
- Security: NFR-021, NFR-023, NFR-024, NFR-025, NFR-027, NFR-028
- Data: NFR-031, NFR-032, NFR-035, NFR-036, NFR-037
- Quality: NFR-041, NFR-042, NFR-045, NFR-046, NFR-048, NFR-071, NFR-072, NFR-073, NFR-074, NFR-081, NFR-082, NFR-091, NFR-092
- Observability: NFR-051, NFR-052, NFR-053, NFR-054
- Tech Stack: NFR-061 to NFR-066

### Should Have (Important)

**Functional Requirements (12):**
- Position: FR-013
- Import: FR-029, FR-031
- Manual Entry: FR-043
- Price: FR-056, FR-057
- Reconciliation: FR-061 to FR-065
- Account: FR-072

**Non-Functional Requirements (10):**
- Deployment: NFR-003, NFR-006, NFR-007, NFR-008, NFR-009
- Performance: NFR-014
- Security: NFR-022, NFR-026
- Data: NFR-033, NFR-034
- Tech Stack: NFR-067, NFR-068

### Could Have (Nice to Have)

**Functional Requirements (2):**
- Reconciliation: FR-066
- Metrics: FR-088

**Non-Functional Requirements (3):**
- Performance: NFR-011
- Quality: NFR-043, NFR-044, NFR-047

---

## Requirements Coverage by Category

### Functional Requirements by Category

| Category | Count | Must Have | Should Have | Could Have |
|----------|-------|-----------|-------------|------------|
| Portfolio Management | 6 | 6 | 0 | 0 |
| Position Management | 5 | 3 | 1 | 0 |
| Data Import | 11 | 9 | 2 | 0 |
| Manual Entry | 6 | 5 | 1 | 0 |
| Price Management | 7 | 5 | 2 | 0 |
| Reconciliation | 6 | 0 | 5 | 1 |
| Account Management | 2 | 1 | 1 | 0 |
| Metrics & Calculations | 13 | 12 | 0 | 1 |
| System-Level | 6 | 6 | 0 | 0 |
| **Total** | **57** | **43** | **12** | **2** |

### Non-Functional Requirements by Category

| Category | Count | Must Have | Should Have | Could Have |
|----------|-------|-----------|-------------|------------|
| Deployment & Infrastructure | 10 | 5 | 5 | 0 |
| Performance & Scalability | 6 | 3 | 1 | 1 |
| Security | 8 | 6 | 2 | 0 |
| Data Management | 7 | 5 | 2 | 0 |
| Quality Attributes | 20 | 14 | 0 | 3 |
| Observability | 4 | 4 | 0 | 0 |
| Technology Stack | 8 | 6 | 2 | 0 |
| **Total** | **48** | **37** | **10** | **3** |

---

## Test Coverage Mapping

### Requirements with Cucumber Scenarios

| Requirement ID | Feature File | Scenario Name |
|----------------|--------------|---------------|
| FR-001 | portfolio-viewing.feature | View total portfolio value |
| FR-002 | portfolio-viewing.feature | View portfolio with positive returns |
| FR-003 | portfolio-viewing.feature | View portfolio with negative returns |
| FR-004 | portfolio-viewing.feature | View empty portfolio |
| FR-005 | portfolio-viewing.feature | View aggregated positions across accounts |
| FR-011 | position-details.feature | View individual stock position |
| FR-012 | position-details.feature | View ETF position with loss |
| FR-013 | position-details.feature | View Polish government bond position |
| FR-014 | position-details.feature | List all positions |
| FR-021 to FR-031 | data-import.feature | Various import and validation scenarios |
| FR-041 to FR-046 | manual-entry.feature | Various manual entry scenarios |
| FR-051 to FR-057 | price-updates.feature | Various price update scenarios |
| FR-061 to FR-066 | reconciliation.feature | Various reconciliation scenarios |

**Cucumber Coverage:** 41 of 57 functional requirements (72%)

**Note:** Remaining 16 FRs are calculation/system requirements tested via unit tests

---

## Dependencies Analysis

### Critical Path Requirements (no dependencies)

**Functional:**
- FR-004, FR-041, FR-042, FR-081, FR-082, FR-089, FR-091, FR-092, FR-094, FR-095, FR-096

**Non-Functional:**
- All NFRs for v0.1 can be implemented in parallel

### High-Dependency Requirements

**Most Referenced Requirements:**
- FR-031 (Current Value Calculation) - Referenced by 5 requirements
- FR-032 (Invested Amount Calculation) - Referenced by 5 requirements
- FR-021 (Import from Broker File) - Referenced by 10 requirements
- FR-051 (Manual Price Update) - Referenced by 6 requirements
- FR-061 (Successful Reconciliation) - Referenced by 5 requirements

---

## Related Documents

- **Functional Requirements:** /Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/functional-requirements.md
- **Non-Functional Requirements:** /Users/michalrzodkiewicz/private/investments-tracker/requirements/non-functional/non-functional-requirements.md
- **Version Roadmap:** /Users/michalrzodkiewicz/private/investments-tracker/planning/VERSION-ROADMAP.md
- **Cucumber Features:** /Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/features/*.feature
- **Domain Language:** /Users/michalrzodkiewicz/private/investments-tracker/requirements/functional/ubiquitous-language.md

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
| 1.0 | 2025-11-10 | Claude | Initial version based on functional and non-functional requirements |
