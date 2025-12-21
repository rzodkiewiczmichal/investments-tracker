# Non-Functional Requirements (NFR)

**Project:** Investment Tracker
**Version:** 1.0
**Last Updated:** 2025-10-31

---

## Executive Summary

The Investment Tracker is a web-based application designed for personal investment portfolio management across multiple broker accounts. The system prioritizes **code quality, clean architecture, and comprehensive testing** as learning objectives, while delivering functional software for personal use. The application will be built **cloud-ready from day one**, initially deployed locally and later migrated to cloud infrastructure.

### Project Goals (by priority)
1. **Highest Priority:** Practice clean documentation, clean architecture (DDD/Hexagonal), clean code, and automated testing
2. **Medium Priority:** Deliver working software with basic functionalities
3. **Lowest Priority:** Frontend polish and user experience

---

## 1. Deployment Architecture

### NFR-001: Web Application Deployment
**Category:** Deployment & Infrastructure
**Priority:** Must Have
**Description:** Application must be deployable as a web application accessible via browser.
**Target Version:** v0.1

### NFR-002: Cloud-Ready Architecture
**Category:** Deployment & Infrastructure
**Priority:** Must Have
**Description:** Application architecture must be designed from day one to support cloud deployment, even though initial deployment is local.
**Target Version:** v0.1 (design), v2.0 (implementation)

### NFR-003: Modern Deployment Practices
**Category:** Deployment & Infrastructure
**Priority:** Must Have
**Description:** Application must support modern deployment practices including Terraform for Infrastructure as Code and Kubernetes for orchestration.
**Target Version:** v2.0

### 1.1 Application Type
- **Web Application** accessed via browser (NFR-001)
- Initially runs locally, designed to be cloud-ready (NFR-002)
- Modern deployment practices (Terraform, Kubernetes) (NFR-003)

### 1.2 Deployment Phases

### NFR-004: Local Deployment Support
**Category:** Deployment & Infrastructure
**Priority:** Must Have
**Description:** Application must be deployable on local machine with PostgreSQL and OTLP server running locally.
**Target Version:** v0.1

### NFR-005: No Authentication for Local Deployment
**Category:** Deployment & Infrastructure
**Priority:** Must Have
**Description:** Local deployment does not require authentication (single-user, trusted environment).
**Target Version:** v0.1

#### Phase 1: Local Deployment
- Application runs on local machine (NFR-004)
- PostgreSQL database on local machine (NFR-004)
- OTLP server on local machine for observability (NFR-004)
- No authentication required (single-user, trusted environment) (NFR-005)
- **Requirement:** Must be architected for easy cloud migration (NFR-002)

### NFR-006: Cloud Deployment Support
**Category:** Deployment & Infrastructure
**Priority:** Should Have
**Description:** Application must support cloud deployment with Terraform IaC and Kubernetes orchestration.
**Target Version:** v2.0

### NFR-007: Cloud Authentication
**Category:** Deployment & Infrastructure
**Priority:** Should Have
**Description:** Cloud deployment must include authentication mechanism (specific mechanism to be decided).
**Target Version:** v2.0

### NFR-008: Encrypted Cloud Connections
**Category:** Deployment & Infrastructure
**Priority:** Should Have
**Description:** Cloud deployment must use TLS/HTTPS for all connections.
**Target Version:** v2.0

### NFR-009: Cloud Database
**Category:** Deployment & Infrastructure
**Priority:** Should Have
**Description:** Cloud deployment must use cloud-managed database service.
**Target Version:** v2.0

#### Phase 2A: Cloud Deployment (Basic Features)
- Migrate Phase 1 features to cloud infrastructure (NFR-006)
- Modern cloud deployment (Terraform for IaC, Kubernetes for orchestration) (NFR-003)
- Add authentication mechanism (to be designed) (NFR-007)
- Encrypted connections (TLS/HTTPS) (NFR-008)
- Cloud database service (NFR-009)

#### Phase 2B: Advanced Features (Local Alternative)
- Extend Phase 1 with advanced features while remaining local
- Alternative development path to Phase 2A

### NFR-010: Browser Compatibility
**Category:** Deployment & Infrastructure
**Priority:** Must Have
**Description:** Application must be accessible via modern web browsers (Chrome, Firefox, Safari, Edge).
**Target Version:** v0.1

### 1.3 Target Platform
- **Environment:** Web-based, platform-independent (NFR-001)
- **Browser Requirements:** Modern browsers (Chrome, Firefox, Safari, Edge) (NFR-010)
- **Network:** Requires internet connectivity (online application)

### NFR-011: Availability for Personal Use
**Category:** Performance & Scalability
**Priority:** Could Have
**Description:** Uptime is not critical for personal use; acceptable downtime and delayed recovery are acceptable.
**Target Version:** v0.1

### 1.4 Availability
- **Uptime:** Not critical; acceptable downtime for personal use (NFR-011)
- **Recovery Time:** Not critical; can tolerate delays in restoration (NFR-011)

---

## 2. Scalability & Performance

### NFR-012: Data Volume Scalability
**Category:** Performance & Scalability
**Priority:** Must Have
**Description:** System must efficiently handle up to 10 broker accounts, 500 positions, and 100 transactions per month with full historical data retention.
**Target Version:** v1.0 (tested at scale)

### 2.1 Data Volume Requirements

| Metric | Current | 1-2 Year Growth |
|--------|---------|-----------------|
| Broker Accounts | Up to 10 | Up to 10 |
| Positions (Instruments) | Up to 100 | Up to 500 |
| Transactions/Month | Up to 100 | Up to 100 |
| Historical Data Retention | Forever | Forever |

### NFR-013: Single User System
**Category:** Performance & Scalability
**Priority:** Must Have
**Description:** System is designed for single user only; no multi-user access control or concurrent user support required.
**Target Version:** v0.1

### NFR-014: Reasonable Performance
**Category:** Performance & Scalability
**Priority:** Should Have
**Description:** System should provide reasonable performance for personal use; strict response time SLAs are not required.
**Target Version:** v0.1

### NFR-015: Daily Price Updates
**Category:** Performance & Scalability
**Priority:** Must Have
**Description:** System must support price updates once per day (scheduled or manual); real-time updates not required.
**Target Version:** v0.4 (manual), v2.1 (automated)

### 2.2 Concurrent Users
- **Single user only** (personal application) (NFR-013)
- No multi-user access control required (NFR-013)

### 2.3 Performance Expectations
- **Response Times:** Not critical; reasonable performance acceptable (NFR-014)
  - Portfolio view loading: No strict requirement
  - Position details loading: No strict requirement
  - CSV import (100 positions): No strict requirement
  - Bulk price updates: No strict requirement
- **Price Refresh Frequency:** Once per day (scheduled or manual) (NFR-015)

### NFR-016: Architecture for Growth
**Category:** Performance & Scalability
**Priority:** Must Have
**Description:** Architecture must support future growth to 500 positions, full transaction history retention, and efficient historical data querying.
**Target Version:** v0.1 (design), v1.0 (validation)

### 2.4 Design for Scale
- While current volumes are modest, architecture should support: (NFR-016)
  - Growth to 500 positions
  - Full transaction history retention
  - Efficient querying of historical data

---

## 3. Security Requirements

### NFR-021: No Local Authentication
**Category:** Security
**Priority:** Must Have
**Description:** Local deployment does not require authentication due to trusted environment.
**Target Version:** v0.1

### NFR-022: Cloud Authentication Required
**Category:** Security
**Priority:** Should Have
**Description:** Cloud deployment requires authentication mechanism (specific method to be decided).
**Target Version:** v2.0

### NFR-023: Single User Access
**Category:** Security
**Priority:** Must Have
**Description:** System supports single user only; no multi-user roles or access control.
**Target Version:** v0.1

### 3.1 Authentication & Authorization

#### Phase 1 (Local)
- **No authentication required** (single-user, trusted local environment) (NFR-021)
- Application assumes trusted local access (NFR-023)

#### Phase 2A (Cloud)
- **Authentication required** (mechanism TBD) (NFR-022)
- Single-user access (no multi-user roles) (NFR-023)

### NFR-024: Data Sensitivity Classification
**Category:** Security
**Priority:** Must Have
**Description:** All financial data is classified as highly sensitive and must be protected accordingly.
**Target Version:** v0.1

### 3.2 Data Sensitivity
- **Classification:** Highly sensitive financial data (NFR-024)
- **All data considered sensitive** for simplicity (NFR-024)
- Future refinement: Create data sensitivity classification (tracked separately in GitHub)

### NFR-025: Local HTTP Acceptable
**Category:** Security
**Priority:** Must Have
**Description:** HTTP connections are acceptable for local deployment (localhost only).
**Target Version:** v0.1

### NFR-026: Cloud HTTPS Mandatory
**Category:** Security
**Priority:** Should Have
**Description:** HTTPS/TLS is mandatory for all cloud deployment connections.
**Target Version:** v2.0

### NFR-027: Data Encryption at Rest
**Category:** Security
**Priority:** Must Have
**Description:** All data must be encrypted at rest using database-level encryption, including account information, positions, transactions, portfolio values, and price history.
**Target Version:** v0.1

### 3.3 Data Encryption

#### Data in Transit
- **Phase 1 (Local):** HTTP acceptable for localhost (NFR-025)
- **Phase 2A (Cloud):** HTTPS/TLS mandatory (NFR-026)

#### Data at Rest
- **All data encrypted at rest** (database-level encryption) (NFR-027)
- Includes:
  - Account information
  - Position data
  - Transaction history
  - Aggregated portfolio values
  - Price history

### NFR-028: Complete Audit Trail
**Category:** Security
**Priority:** Must Have
**Description:** System must maintain complete audit trail for all data modifications, tracking user, action, timestamp, and affected entity. Audit logs must be retained indefinitely.
**Target Version:** v0.1

### 3.4 Audit Trail
- **Full audit trail required** for all data modifications (NFR-028)
- Track: who (user), what (action), when (timestamp), which (entity) (NFR-028)
- Audit logs must be retained indefinitely (NFR-028)

---

## 4. Reliability & Data Integrity

### NFR-031: Zero Data Loss Tolerance
**Category:** Data Management
**Priority:** Must Have
**Description:** System must have zero tolerance for data loss; data loss is catastrophic and cannot be recreated. Robust data persistence and integrity mechanisms required.
**Target Version:** v0.1

### NFR-032: Indefinite Data Retention
**Category:** Data Management
**Priority:** Must Have
**Description:** System must retain all historical data forever with no automatic purging or archiving. Full transaction history required for performance tracking.
**Target Version:** v0.1

### 4.1 Data Loss Tolerance
- **Zero tolerance:** Data loss is catastrophic and cannot be recreated (NFR-031)
- **Implication:** Robust data persistence and integrity mechanisms required (NFR-031)

### 4.2 Data Retention
- **Policy:** Retain all historical data forever (NFR-032)
- **Transaction History:** Full history required (important for performance tracking, tax reporting potential) (NFR-032)
- **No automatic data purging or archiving** (NFR-032)

### NFR-033: Manual Backup for Local Deployment
**Category:** Data Management
**Priority:** Should Have
**Description:** Local deployment does not require automated backups; user is responsible for database backups. Application must document backup procedures.
**Target Version:** v0.1

### NFR-034: Automated Cloud Backups
**Category:** Data Management
**Priority:** Should Have
**Description:** Cloud deployment must utilize cloud provider backup mechanisms and automated database snapshots.
**Target Version:** v2.0

### NFR-035: ACID Compliance
**Category:** Data Management
**Priority:** Must Have
**Description:** System must provide ACID compliance for all financial transactions. No eventual consistency acceptable for critical financial data.
**Target Version:** v0.1

### 4.3 Backup & Recovery

#### Phase 1 (Local)
- No automated backup system required (NFR-033)
- User responsible for backing up database files (NFR-033)
- Application should document backup procedures (NFR-033)

#### Phase 2A (Cloud)
- Cloud provider's backup mechanisms sufficient (NFR-034)
- Consider automated database snapshots (NFR-034)

### 4.4 Data Consistency
- **ACID compliance required** for all financial transactions (NFR-035)
- No eventual consistency for critical financial data (NFR-035)
- Database transactions must ensure data integrity (NFR-035)

---

## 5. Data Persistence

### NFR-036: PostgreSQL Database
**Category:** Data Management
**Priority:** Must Have
**Description:** System must use PostgreSQL as server-based relational database. Embedded databases (SQLite, H2) are not acceptable for production use.
**Target Version:** v0.1

### NFR-037: Database Transaction Support
**Category:** Data Management
**Priority:** Must Have
**Description:** Database must support ACID transactions, encryption at rest, efficient indexing for historical queries, and foreign key constraints for referential integrity.
**Target Version:** v0.1

### 5.1 Database Type
- **Server-based relational database** (NFR-036)
- **Preferred:** PostgreSQL (open-source, robust, excellent for financial data) (NFR-036)
- **Not acceptable:** Embedded databases (SQLite, H2) for production use (NFR-036)

### 5.2 Database Characteristics
- Support for transactions (ACID) (NFR-037)
- Support for encryption at rest (NFR-037)
- Efficient indexing for historical queries (NFR-037)
- Foreign key constraints for referential integrity (NFR-037)

---

## 6. Usability & User Experience

### NFR-041: Angular Frontend
**Category:** Quality Attributes
**Priority:** Must Have
**Description:** System must use Angular framework for web-based UI.
**Target Version:** v0.1

### NFR-042: Modern Simple UI
**Category:** Quality Attributes
**Priority:** Must Have
**Description:** UI must be modern, attractive, and simple/functional (both rated critical 5/5).
**Target Version:** v0.1

### NFR-043: No Mobile Responsiveness Required
**Category:** Quality Attributes
**Priority:** Could Have
**Description:** Mobile-responsive design is not important for personal desktop use (rated 1/5).
**Target Version:** Future

### NFR-044: No Accessibility Requirements
**Category:** Quality Attributes
**Priority:** Could Have
**Description:** Accessibility standards are not required for personal use (rated 1/5).
**Target Version:** Future

### NFR-045: Developer-Intuitive Interface
**Category:** Quality Attributes
**Priority:** Must Have
**Description:** Interface must be intuitive for the developer (owner); no requirement for external user learning curve.
**Target Version:** v0.1

### 6.1 User Interface
- **Web-based UI** (HTML/CSS/JavaScript) (NFR-041)
- **Frontend Framework:** Angular (NFR-041)

### 6.2 UX Priorities (rated 1-5, 5 = critical)
- **Modern, attractive UI:** 5/5 (critical) (NFR-042)
- **Simple, functional UI:** 5/5 (critical) (NFR-042)
- **Mobile-responsive:** 1/5 (not important for personal desktop use) (NFR-043)
- **Accessibility:** 1/5 (not important for personal use) (NFR-044)

### 6.3 Learning Curve
- **Target:** Intuitive for the developer (owner) (NFR-045)
- No requirement for learning by external users (NFR-045)

---

## 7. Integration & Extensibility

### NFR-046: CSV Import Support
**Category:** Quality Attributes
**Priority:** Must Have
**Description:** System must support CSV import for position data from multiple broker formats (6 formats) and price updates with comprehensive validation.
**Target Version:** v0.2 (first format), v0.5 (all 6 formats)

### NFR-047: No Export Required for MVP
**Category:** Quality Attributes
**Priority:** Could Have
**Description:** Export capabilities are not required for MVP.
**Target Version:** Future

### NFR-048: Extensible Architecture
**Category:** Quality Attributes
**Priority:** Must Have
**Description:** Architecture must remain open for future integrations and extensions.
**Target Version:** v0.1

### 7.1 Price Data Sources
- **Phase 1:** CSV file import with prices (manual process) (NFR-046)
- **Future:** No immediate plans for API integration

### 7.2 Export Capabilities
- **Not required** for MVP (NFR-047)

### 7.3 Import Capabilities
- **CSV import required** for: (NFR-046)
  - Position data from multiple broker formats (6 different formats documented)
  - Price updates
- **Validation:** Comprehensive validation rules (documented in Cucumber features)

### 7.4 Future Integrations
- **None planned currently**
- Architecture should remain open for future extension (NFR-048)

---

## 8. Monitoring & Observability

### NFR-051: OTLP Observability
**Category:** Observability
**Priority:** Must Have
**Description:** System must implement OpenTelemetry Protocol (OTLP) for distributed tracing across all application layers with local OTLP server for trace ingestion.
**Target Version:** v0.1

### NFR-052: Detailed Logging
**Category:** Observability
**Priority:** Must Have
**Description:** System must provide detailed logs for troubleshooting and learning purposes.
**Target Version:** v0.1

### NFR-053: User-Friendly Error Messages
**Category:** Observability
**Priority:** Must Have
**Description:** User-facing error messages must be friendly and clear; detailed errors and stack traces sent to OTLP collector only (not exposed to user).
**Target Version:** v0.1

### NFR-054: Hexagonal Architecture Tracing
**Category:** Observability
**Priority:** Must Have
**Description:** System must provide ability to trace requests through all hexagonal architecture layers for learning and debugging.
**Target Version:** v0.1

### 8.1 Logging & Tracing
- **Standard:** OpenTelemetry Protocol (OTLP) (NFR-051)
- **Local OTLP Server:** Running on local machine to ingest traces (NFR-051)
- **Log Level:** Detailed logs for troubleshooting and learning purposes (NFR-052)
- **Distributed Tracing:** Implement OTLP tracing across application layers (NFR-051)

### 8.2 Error Handling
- **User-Facing:** User-friendly error messages only (NFR-053)
- **System-Level:** Detailed error logs and traces sent to OTLP collector (NFR-053)
- **No stack traces exposed to end user** (NFR-053)

### 8.3 Observability Goals
- **Primary:** Learn modern observability practices (OTLP) (NFR-054)
- **Secondary:** Debug and troubleshoot issues during development (NFR-054)
- Ability to trace requests through hexagonal architecture layers (NFR-054)

---

## 9. Technology Stack Constraints

### NFR-061: Java Backend
**Category:** Technology Stack
**Priority:** Must Have
**Description:** Backend must be implemented in native Java (modern version TBD in technical requirements).
**Target Version:** v0.1

### NFR-062: Minimal Spring Boot Usage
**Category:** Technology Stack
**Priority:** Must Have
**Description:** Spring Boot usage must be minimal, only where absolutely necessary.
**Target Version:** v0.1

### NFR-063: Hexagonal Architecture
**Category:** Technology Stack
**Priority:** Must Have
**Description:** System must implement Hexagonal (Ports & Adapters) architecture with clear separation: Domain Core (pure Java, no framework), Application Layer (use cases, minimal framework), Infrastructure Layer (Spring Boot, database, external systems).
**Target Version:** v0.1

### NFR-064: Framework-Independent Domain
**Category:** Technology Stack
**Priority:** Must Have
**Description:** Domain core must be pure Java with no framework dependencies; framework changes must not affect domain logic.
**Target Version:** v0.1

### NFR-065: Angular Frontend
**Category:** Technology Stack
**Priority:** Must Have
**Description:** Frontend must use Angular framework with focus on functional UI over aesthetic perfection (lowest priority for polish).
**Target Version:** v0.1

### NFR-066: Docker Containerization
**Category:** Technology Stack
**Priority:** Must Have
**Description:** Application must be containerized using Docker for both local and cloud deployment.
**Target Version:** v0.1

### NFR-067: Terraform Infrastructure as Code
**Category:** Technology Stack
**Priority:** Should Have
**Description:** Cloud infrastructure must be provisioned using Terraform (Phase 2A).
**Target Version:** v2.0

### NFR-068: Kubernetes Orchestration
**Category:** Technology Stack
**Priority:** Should Have
**Description:** Cloud deployment must use Kubernetes for container orchestration (Phase 2A).
**Target Version:** v2.0

### 9.1 Backend

#### Core Requirements
- **Language:** Native Java (modern version TBD in technical requirements) (NFR-061)
- **Spring Boot:** Minimal usage, only where absolutely necessary (NFR-062)
- **Architecture:** Hexagonal (Ports & Adapters) architecture (NFR-063)
  - Clear separation between:
    - **Domain Core:** Pure Java, no framework dependencies (NFR-064)
    - **Infrastructure Layer:** Spring Boot, database, external systems
    - **Application Layer:** Use cases, minimal framework coupling

#### Rationale
- Demonstrate clean architecture principles (NFR-063)
- Decouple business logic from framework (NFR-064)
- Framework changes should not affect domain logic (NFR-064)

### 9.2 Frontend
- **Framework:** Angular (NFR-065)
- **Priority:** Lowest priority for polish and features (NFR-065)
- Focus on functional UI, not aesthetic perfection (NFR-065)

### 9.3 Infrastructure as Code
- **Terraform:** For cloud infrastructure provisioning (Phase 2A) (NFR-067)
- **Kubernetes:** For container orchestration (Phase 2A) (NFR-068)
- **Docker:** For containerization (both phases) (NFR-066)

---

## 10. Quality Attributes

### NFR-071: Clean Code Quality
**Category:** Quality Attributes
**Priority:** Must Have (5/5 - Critical)
**Description:** Code must be clean and maintainable following SOLID principles, Domain-Driven Design patterns, and clear separation of concerns via Hexagonal Architecture.
**Target Version:** v0.1

### NFR-072: Comprehensive Test Coverage
**Category:** Quality Attributes
**Priority:** Must Have (5/5 - Critical)
**Description:** System must have comprehensive automated tests including Cucumber BDD for acceptance criteria, unit tests for domain logic, integration tests for infrastructure, and architecture tests to enforce hexagonal boundaries. Test-Driven Development encouraged.
**Target Version:** v0.1 (70%+), v1.0 (85%+)

### NFR-073: Complete Documentation
**Category:** Quality Attributes
**Priority:** Must Have (5/5 - Critical)
**Description:** Code must be fully documented with Javadoc for public APIs, Architecture Decision Records (ADRs), domain model documentation, and ubiquitous language glossary. User documentation has lower priority.
**Target Version:** v0.1

### NFR-074: Quality Over Speed
**Category:** Quality Attributes
**Priority:** Must Have (3/5 - Medium)
**Description:** Development must balance speed with quality; quality and learning take precedence over delivery speed ("Make it right, then make it fast").
**Target Version:** v0.1

### 10.1 Code Quality (5/5 - Critical)
- **Clean, maintainable code:** Highest priority (NFR-071)
- **SOLID principles** (NFR-071)
- **Domain-Driven Design patterns** (NFR-071)
- **Clear separation of concerns (Hexagonal Architecture)** (NFR-071)

### 10.2 Test Coverage (5/5 - Critical)
- **Comprehensive automated tests:** (NFR-072)
  - **Cucumber BDD tests** for acceptance criteria
  - **Unit tests** for domain logic
  - **Integration tests** for infrastructure
  - **Architecture tests** to enforce hexagonal boundaries
- **Test-Driven Development (TDD) encouraged** (NFR-072)

### 10.3 Documentation (5/5 - Critical)
- **Code documentation:** (NFR-073)
  - Javadoc for public APIs
  - Architecture Decision Records (ADRs)
  - Domain model documentation
  - Ubiquitous language glossary (already exists)
- **User documentation:** Lower priority (NFR-073)

### 10.4 Development Speed (3/5 - Medium)
- Balance speed with quality (NFR-074)
- Quality and learning take precedence over delivery speed (NFR-074)
- "Make it right, then make it fast" (NFR-074)

---

## 11. Development Priorities (Ranked)

1. **Code Quality** - Clean, maintainable, well-tested code (DDD, Hexagonal Architecture)
2. **Fast Development** - Reasonable development velocity
3. **Features** - Rich functionality
4. **Performance** - Fast response times
5. **User Experience** - Polished UI

---

## 12. Compliance & Legal

### NFR-081: No Privacy Compliance Required
**Category:** Quality Attributes
**Priority:** Must Have (as exclusion)
**Description:** System is for personal use only; not subject to GDPR, CCPA, or other data privacy regulations.
**Target Version:** v0.1

### NFR-082: No Data Residency Restrictions
**Category:** Quality Attributes
**Priority:** Must Have (as exclusion)
**Description:** System has no restrictions on data location.
**Target Version:** v0.1

### 12.1 Data Privacy Regulations
- **No compliance requirements** (personal use only) (NFR-081)
- Not subject to GDPR, CCPA, or other regulations (NFR-081)

### 12.2 Data Residency
- **No restrictions** on data location (NFR-082)

---

## 13. Cost & Budget

### NFR-091: Zero Infrastructure Budget
**Category:** Quality Attributes
**Priority:** Must Have
**Description:** Infrastructure costs must be $0 (free/self-hosted only). Local deployment is free; cloud deployment must minimize costs using free tiers.
**Target Version:** v0.1 (local), v2.0 (cloud)

### NFR-092: Zero Third-Party Service Budget
**Category:** Quality Attributes
**Priority:** Must Have
**Description:** Third-party service costs must be $0 (free services only). No budget for paid APIs (price data, etc.).
**Target Version:** v0.1

### 13.1 Infrastructure Costs
- **Budget:** $0 (free/self-hosted only) (NFR-091)
- **Phase 1:** Free (local deployment) (NFR-091)
- **Phase 2A:** Minimize cloud costs; use free tiers where possible (NFR-091)

### 13.2 Third-Party Services
- **Budget:** $0 (free services only) (NFR-092)
- No budget for paid APIs (price data, etc.) (NFR-092)

---

## 14. Maintainability & Extensibility

### 14.1 Maintenance
- **Owner:** Single developer (project creator)
- **Code style:** Consistent, idiomatic Java
- **Dependencies:** Minimize external dependencies; prefer standard library

### 14.2 Extensibility
- **Hexagonal Architecture:** Enables easy addition of new adapters
- **Plugin points:**
  - New import format adapters
  - New price data sources
  - New export formats
  - New UI views

### 14.3 Upgradability
- Keep dependencies up-to-date
- Design for framework upgradability (e.g., Spring Boot version changes)
- Domain core should be framework-agnostic

---

## 15. Timeline & Delivery

### 15.1 MVP Timeline
- **No strict deadline**
- Quality and learning take precedence over speed
- Iterative development with regular commits

### 15.2 Development Approach
- **Iterative and incremental**
- Focus on vertical slices of functionality
- Each slice fully tested before moving to next

---

## 16. Key Constraints & Trade-offs

### 16.1 Critical Constraints
1. **Technology Stack:** Native Java, minimal Spring Boot, Angular frontend
2. **Architecture:** Hexagonal architecture is non-negotiable
3. **Testing:** BDD with Cucumber is mandatory
4. **Observability:** OTLP standard required

### 16.2 Acceptable Trade-offs
1. **Performance vs. Code Quality:** Choose quality
2. **Features vs. Test Coverage:** Choose coverage
3. **UI Polish vs. Functionality:** Choose functionality
4. **Development Speed vs. Architecture:** Choose architecture

### 16.3 Non-Negotiable Requirements
1. Full transaction history retention
2. Data encryption at rest
3. Full audit trail
4. Zero data loss tolerance
5. Hexagonal architecture pattern
6. Comprehensive test coverage

---

## 17. Open Questions & Future Decisions

### 17.1 To Be Decided Later
1. **Authentication mechanism for cloud deployment** (Phase 2A)
2. **Specific Java version** (to be defined in technical requirements)
3. **Spring Boot version and modules** (minimize usage)
4. **Cloud provider selection** (AWS, Azure, GCP, or self-hosted Kubernetes)
5. **Data sensitivity classification levels** (tracked in GitHub issue)

### 17.2 Future Roadmap Items
- Define specific broker import format specifications (when actual samples available)
- Explore price data API options (free APIs)
- Consider future multi-currency support enhancements

---

## 18. Related Documents

- **Functional Requirements:** `requirements/functional/functional-requirements-interview.md`
- **Domain Language:** `requirements/functional/ubiquitous-language.md`
- **User Personas:** `requirements/functional/user-personas.md`
- **BDD Scenarios:** `requirements/functional/features/*.feature` (6 feature files)
- **Import Formats:** `requirements/functional/import-formats-todo.md`
- **Project History:** `PROJECT-HISTORY.md`

---

## 19. Approval & Sign-off

**Status:** Draft
**Reviewed by:** Pending
**Approved by:** Pending
**Date:** 2025-10-31

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-10-31 | Claude | Initial version based on NFR questionnaire |
