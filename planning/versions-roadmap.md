# Investment Tracker - Version Roadmap

**Document Version:** 1.1
**Last Updated:** 2025-11-10
**Status:** Complete with Requirements Traceability

---

## Executive Summary

This roadmap defines a clear versioning strategy that resolves conflicts between MVP simplicity and future feature richness. The strategy prioritizes **learning goals** (DDD, Hexagonal Architecture, Cucumber BDD) while delivering working software incrementally.

### Key Principles

1. **Start Ultra-Minimal:** MVP focuses on one complete vertical slice to prove architecture
2. **Incremental Feature Addition:** Each version adds 1-3 related features
3. **Architecture First:** Every version maintains clean architecture and comprehensive tests
4. **Learning Over Speed:** Quality and educational value trump delivery speed
5. **Cloud-Ready Design:** Architecture supports local deployment now, cloud migration later

### Version Philosophy

- **v0.1-0.x:** Local deployment, building features iteratively
- **v1.0:** Feature-complete for local personal use
- **v2.0+:** Cloud deployment and advanced features

---

## Version Definitions

### MVP (v0.1) - "Domain Foundation"

**Goal:** Prove the core architecture and domain model with one complete vertical slice

**Functional Scope:**
- Manual entry of positions (stock/ETF only, no bonds yet)
- Single account support
- View portfolio with basic metrics:
  - Total current value
  - Total invested amount
  - Total P&L (amount and percentage)
- View individual position details with same metrics
- Store minimal transaction data (buy date, quantity, price) for future XIRR

**Requirements in Scope:** 20 Functional Requirements + 37 Non-Functional Requirements

> See `planning/requirements-by-version.md` for complete requirement ID mapping

**Key Capabilities:**
- Manual position entry (stocks/ETFs only)
- Portfolio viewing with P&L metrics (no XIRR yet)
- Single account support
- Hexagonal architecture foundation

**Out of Scope:**
- Multi-account features (deferred to v0.2)
- XIRR calculations (deferred to v0.3)
- CSV import (deferred to v0.2)
- Price management (deferred to v0.4)
- Polish government bonds (deferred to v0.6)
- Reconciliation (deferred to v0.7)

**Non-Functional Scope:**
- Hexagonal architecture fully implemented
- PostgreSQL database with encryption at rest
- Web UI (Angular) with one screen for portfolio view, one for position entry
- Comprehensive Cucumber tests for implemented scenarios
- Unit tests for domain logic
- Architecture tests to enforce boundaries
- OTLP observability setup (traces through all layers)
- Local deployment only (Docker Compose)
- No authentication (localhost access)

**Domain Model:**
- Account (single account only)
- Instrument (stock/ETF, no bonds)
- Position (aggregated view - even though only one account)
- Transaction (buy only, minimal data)
- Basic financial calculations (current value, P&L)


**Success Criteria:**
- ✅ Can manually enter 3-5 positions
- ✅ Portfolio view shows correct totals
- ✅ All Cucumber scenarios pass (subset implemented)
- ✅ Architecture tests verify hexagonal boundaries
- ✅ OTLP traces visible in local collector
- ✅ Application runs via Docker Compose
- ✅ Code demonstrates clean DDD principles

**Complexity:** Medium (architecture setup is significant)


**Key Learning Objectives:**
- Hexagonal architecture implementation
- DDD aggregate boundaries
- Cucumber BDD workflow
- OTLP tracing through layers

---

### Version 0.2 - "Multi-Account Aggregation"

**Goal:** Prove the core business value - aggregating positions across multiple accounts with real-time pricing

**Functional Scope:**
- Multiple account support
- Position aggregation across accounts (same instrument in different accounts)
- Account-level and aggregated views
- Add first CSV import adapter (choose simplest broker format)
- Automatic price fetching from Yahoo Finance API during import
- Display last price refresh timestamp in portfolio

**Requirements in Scope:** 18 new FRs + 1 new NFR (38 FRs total, 38 NFRs total)

> See `planning/requirements-by-version.md` for complete requirement ID mapping

**Key Capabilities:**
- Multi-account position aggregation
- First CSV import format support
- Yahoo Finance API integration for current prices
- Price validation and error handling
- Account-level and aggregated portfolio views
- Enhanced average cost calculation across accounts
- Last price refresh date display

**Out of Scope:**
- XIRR calculations (deferred to v0.3)
- Additional broker formats (deferred to v0.5)
- Automated daily price updates (deferred to v2.1)
- Polish government bonds (deferred to v0.6)
- Reconciliation (deferred to v0.7)

**Non-Functional Scope:**
- Import validation framework
- Yahoo Finance API integration
- API error handling and retry logic
- Enhanced test coverage for aggregation logic
- Performance testing with 50+ positions

**Domain Model Changes:**
- Multi-account position aggregation logic
- Import domain service
- Broker format adapter pattern
- Price value object with timestamp
- Yahoo Finance API adapter (port & adapter)


**Success Criteria:**
- ✅ Can track positions in 3-5 different accounts
- ✅ Same instrument in multiple accounts aggregates correctly
- ✅ Can import positions from 1 broker format
- ✅ Prices automatically fetched from Yahoo Finance during import
- ✅ Portfolio displays last price refresh timestamp
- ✅ Import validation catches all error cases
- ✅ API errors handled gracefully with clear error messages
- ✅ Average cost calculation works correctly across accounts

**Complexity:** Medium-High (added API integration)


**Key Learning Objectives:**
- DDD aggregation patterns
- Adapter pattern for imports and external APIs
- Cross-aggregate calculations
- External API integration and error handling

---

### Version 0.3 - "Performance Tracking (XIRR)"

**Goal:** Add time-weighted performance metrics

**Functional Scope:**
- XIRR calculation for individual positions
- XIRR calculation for total portfolio
- Transaction date tracking (already stored, now used)
- Performance metrics dashboard

**Requirements in Scope:** 4 new FRs + 0 new NFRs (37 FRs total, 38 NFRs total)

> See `planning/requirements-by-version.md` for complete requirement ID mapping

**Key Capabilities:**
- Position-level XIRR calculation
- Portfolio-level XIRR calculation
- Time-weighted performance metrics
- Transaction date tracking fully utilized

**Out of Scope:**
- Price management (deferred to v0.4)
- Polish government bonds (deferred to v0.6)
- Reconciliation (deferred to v0.7)
- Additional broker formats (deferred to v0.5)

**Non-Functional Scope:**
- XIRR algorithm implementation (or library integration)
- Performance testing for XIRR with multiple transactions

**Domain Model Changes:**
- XIRR value object
- Performance calculation domain service
- Transaction date handling


**Success Criteria:**
- ✅ XIRR displays correctly for positions with multiple buys
- ✅ Portfolio XIRR aggregates across all positions
- ✅ XIRR calculation is accurate (verified against Excel)
- ✅ Performance when calculating XIRR for 100+ transactions is acceptable

**Complexity:** Medium-High (XIRR algorithm complexity)


**Key Learning Objectives:**
- Financial calculation implementation
- Domain service patterns
- Algorithm integration

---

### Version 0.4 - "Complete Import Coverage"

**Goal:** Support all 6 broker formats

**Functional Scope:**
- Add remaining broker import adapters (5 more formats)
- Import format selection UI
- Import preview before commit
- Import history tracking

**Requirements in Scope:** 0 new FRs (extends FR-021) + 0 new NFRs (extends NFR-046) (38 FRs total, 38 NFRs total)

> See `planning/requirements-by-version.md` for complete requirement ID mapping

**Key Capabilities:**
- Support for all 6 broker import formats
- Import format detection and selection
- Import preview functionality
- Comprehensive format validation

**Out of Scope:**
- Import duplicate prevention (deferred to v0.7)
- Polish government bonds (deferred to v0.5)
- Reconciliation (deferred to v0.6)

**Non-Functional Scope:**
- Character encoding handling (Polish characters)
- Format detection and validation
- Comprehensive import testing suite

**Domain Model Changes:**
- Complete adapter registry
- Import preview service
- Format detection logic


**Success Criteria:**
- ✅ Can import from all 6 broker account types
- ✅ Format selection is intuitive
- ✅ Preview catches errors before import
- ✅ Import history shows all past imports
- ✅ Duplicate import prevention works

**Complexity:** Medium (repetitive adapter work)


**Key Learning Objectives:**
- Scaling adapter pattern
- Format handling and validation
- Registry pattern

---

### Version 0.5 - "Polish Government Bonds Support"

**Goal:** Support all instrument types user owns

**Functional Scope:**
- Polish government bonds instrument type
- Different handling (held to maturity, no quantity/price split)
- Manual entry for bonds
- Bond value updates

**Requirements in Scope:** 2 new FRs + 0 new NFRs (40 FRs total, 38 NFRs total)

> See `planning/requirements-by-version.md` for complete requirement ID mapping

**Key Capabilities:**
- Polish government bonds support
- Bond-specific valuation (held to maturity)
- Manual entry for bonds (constant pricing logic)
- Complete instrument type coverage

**Out of Scope:**
- Import duplicate prevention (deferred to v0.7)
- Reconciliation (deferred to v0.6)

**Non-Functional Scope:**
- Bond-specific validation rules
- Bond display in portfolio

**Domain Model Changes:**
- Bond instrument subtype
- Bond valuation logic (different from stocks)
- Bond position representation


**Success Criteria:**
- ✅ Can add Polish government bond positions
- ✅ Bond valuation shows invested amount and current value
- ✅ Bonds aggregate correctly in portfolio totals
- ✅ Bond P&L calculation is correct

**Complexity:** Small-Medium

**Key Learning Objectives:**
- Polymorphism in domain model
- Instrument type handling
- Different valuation strategies

---

### Version 0.6 - "Reconciliation"

**Goal:** Data accuracy verification against broker statements

**Functional Scope:**
- Reconciliation process for broker statements
- Quantity and value comparison
- Mismatch detection and reporting
- Reconciliation tolerance settings
- Reconciliation history

**Requirements in Scope:** 5 new FRs + 0 new NFRs (45 FRs total, 38 NFRs total)

> See `planning/requirements-by-version.md` for complete requirement ID mapping

**Key Capabilities:**
- Broker statement reconciliation
- Quantity and value comparison
- Mismatch detection and reporting
- Reconciliation tolerance configuration

**Out of Scope:**
- Reconciliation history view (deferred to v0.7)
- Import duplicate prevention (deferred to v0.7)

**Non-Functional Scope:**
- Reconciliation report generation
- Detailed mismatch analysis

**Domain Model Changes:**
- Reconciliation aggregate
- Comparison service
- Mismatch detection logic


**Success Criteria:**
- ✅ Can reconcile against broker statement data
- ✅ Mismatches are clearly identified
- ✅ Tolerance settings work correctly
- ✅ Reconciliation history is maintained
- ✅ Can drill down into specific mismatches

**Complexity:** Medium


**Key Learning Objectives:**
- Comparison algorithms
- Reporting patterns
- Data quality checks

---

### Version 0.7 - "Feature Complete Local Version"

**Goal:** Production-ready for personal daily use (local deployment)

**Functional Scope:**
- All features from v0.1-0.7 polished and tested
- Bug fixes and refinements
- User documentation
- Backup and restore procedures

**Requirements in Scope:** 2 new FRs + 2 updated NFRs (47 FRs total, 40 NFRs total)

> See `planning/requirements-by-version.md` for complete requirement ID mapping

**Key Capabilities:**
- Import duplicate prevention
- Reconciliation history view
- Production-ready polish and refinement
- Complete test coverage (85%+)
- Performance validated for 500 positions

**Out of Scope:**
- Cloud deployment (deferred to v1.0)
- Automated daily price updates (deferred to v1.1)
- Partial sales tracking (deferred to future)

**Non-Functional Scope:**
- Complete test coverage (>80% code coverage)
- Performance optimized for 500 positions
- UI polish and usability improvements
- Comprehensive error handling
- Complete OTLP observability
- Production-grade logging

**Domain Model Changes:**
- No major changes, refinement only

**Success Criteria:**
- ✅ All 6 Cucumber feature files fully implemented
- ✅ Application handles 500 positions smoothly
- ✅ No critical bugs
- ✅ Documentation complete (user guide, backup procedures)
- ✅ Test coverage >80%
- ✅ Ready for daily personal use

**Complexity:** Medium (polish and refinement)


**Key Learning Objectives:**
- Production readiness
- Documentation practices
- Performance optimization

---

### Version 1.0 - "Cloud Deployment"

**Goal:** Migrate to cloud infrastructure with authentication

**Functional Scope:**
- Same features as v1.0
- Authentication mechanism (OAuth2, basic auth, or other)
- User profile management

**Requirements in Scope:** 0 new FRs + 9 new NFRs (47 FRs total, 49 NFRs total)

> See `planning/requirements-by-version.md` for complete requirement ID mapping

**Key Capabilities:**
- Cloud infrastructure deployment (Terraform + Kubernetes)
- Authentication mechanism
- HTTPS/TLS encryption
- Cloud database migration
- Automated cloud backups
- Production cloud monitoring

**Out of Scope:**
- Automated daily price updates (deferred to v1.1)
- Mobile responsiveness (optional future)
- Accessibility features (optional future)
- Export capabilities (optional future)

**Non-Functional Scope:**
- Cloud infrastructure (Terraform IaC)
- Kubernetes deployment
- HTTPS/TLS encryption
- Cloud database migration
- Automated backups
- Monitoring and alerting
- Cloud OTLP collector

**Domain Model Changes:**
- User identity (if multi-user support added)
- No other changes


**Success Criteria:**
- ✅ Application deployed to cloud
- ✅ HTTPS enabled
- ✅ Authentication working
- ✅ Database migrated to cloud
- ✅ Automated backups configured
- ✅ Infrastructure as code (Terraform)
- ✅ Kubernetes orchestration working

**Complexity:** High (infrastructure work)


**Key Learning Objectives:**
- Cloud architecture
- Terraform and Kubernetes
- Authentication implementation
- DevOps practices

---

### Version 1.1 - "Automated Daily Price Updates"

**Goal:** Automated daily price refresh without user intervention

**Functional Scope:**
- Scheduled daily price updates from Yahoo Finance API
- Background job scheduling
- Price refresh for all instruments
- Error notification and retry logic

**Requirements in Scope:** 0 new FRs + 1 enhanced NFR (47 FRs total, 49 NFRs total)

> See `planning/requirements-by-version.md` for complete requirement ID mapping

**Key Capabilities:**
- Automated daily price refresh (scheduled job)
- API rate limit handling
- Error notification system
- Price update history tracking

**Out of Scope:**
- Partial sales tracking (optional future)
- Mobile responsiveness (optional future)
- Accessibility features (optional future)
- Export capabilities (optional future)

**Non-Functional Scope:**
- API rate limit handling
- Retry logic
- Price source adapter pattern

**Domain Model Changes:**
- Price source adapter
- Scheduled update service

**Success Criteria:**
- ✅ Prices update automatically daily
- ✅ API failures don't break application
- ✅ Can configure price sources per instrument
- ✅ Manual override still available

**Complexity:** Medium


---

### Version 1.2 - "Advanced Analytics"

**Goal:** Portfolio allocation and risk metrics

**Functional Scope:**
- Portfolio allocation by instrument type
- Allocation by account
- Basic risk metrics (if feasible)
- Historical performance charts
- Benchmark comparisons

**Non-Functional Scope:**
- Charting library integration
- Analytics calculation engine

**Domain Model Changes:**
- Analytics services
- Historical tracking


**Success Criteria:**
- ✅ Can see allocation breakdowns
- ✅ Historical performance charts display
- ✅ Basic comparisons to benchmarks work

**Complexity:** Medium-High


---

### Future Versions (2.0+) - Ideas Only

**Potential Features:**
- Multi-currency support (full implementation)
- Tax reporting and realized gains tracking
- Dividend tracking and reinvestment
- Transaction fees and commission tracking
- Multi-user support
- Export functionality (CSV, PDF reports)
- Advanced risk metrics (volatility, beta, correlation)
- Rebalancing recommendations


---

## Version Comparison Matrix

| Feature | v0.1 | v0.2 | v0.3 | v0.4 | v0.5 | v0.6 | v0.7 | v1.0 | v2.0 | v2.1+ |
|---------|------|------|------|------|------|------|------|------|------|-------|
| Manual Position Entry | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Single Account | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Multiple Accounts | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Position Aggregation | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Basic Metrics (P&L) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| XIRR Calculation | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| CSV Import (1 broker) | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| CSV Import (6 brokers) | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Price Updates (Manual) | Basic | Basic | Basic | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Price Updates (CSV) | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Polish Gov Bonds | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Reconciliation | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ |
| Local Deployment | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Cloud Deployment | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| Authentication | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| Automated Prices | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Requirements Count (FR/NFR)** | **20/37** | **33/38** | **37/38** | **42/39** | **42/39** | **45/39** | **50/39** | **54/41** | **54/50** | **54/50** |

---

## Architecture Evolution

### v0.1 Architecture
```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer (Angular)                  │
│              Portfolio View | Position Entry             │
└─────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────┐
│             Application Layer (Use Cases)                │
│     ViewPortfolio | AddPosition | CalculateMetrics       │
└─────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────┐
│                  Domain Layer (Pure Java)                │
│       Position | Instrument | Account | Transaction      │
│              Portfolio | Financial Calculations          │
└─────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────┐
│           Infrastructure Layer (Adapters)                │
│    PostgreSQL Repo | OTLP Tracing | Spring Boot          │
└─────────────────────────────────────────────────────────┘
```

### Key Architectural Principles (All Versions)

1. **Hexagonal Architecture:** Clear separation of domain, application, and infrastructure
2. **Domain-Driven Design:** Rich domain model, ubiquitous language, aggregates
3. **Dependency Rule:** Dependencies point inward (infrastructure → application → domain)
4. **Test Coverage:** Cucumber for acceptance, unit tests for domain, integration tests for adapters
5. **OTLP Observability:** Traces through all layers from day one
6. **Port & Adapter Pattern:** Easy to swap infrastructure (database, price sources, import formats)

---

## Testing Strategy by Version

### v0.1 Testing
- **Cucumber:** Portfolio viewing, position entry scenarios
- **Unit Tests:** P&L calculation, position aggregation (single account)
- **Integration Tests:** PostgreSQL repository, database setup
- **Architecture Tests:** Enforce hexagonal boundaries
- **Target Coverage:** 70%+

### v0.2 Testing
- **Cucumber:** Multi-account aggregation scenarios, first import format
- **Unit Tests:** Multi-account aggregation logic, average cost calculation
- **Integration Tests:** CSV import adapter
- **Target Coverage:** 75%+

### v0.3 Testing
- **Cucumber:** XIRR calculation scenarios
- **Unit Tests:** XIRR algorithm, transaction date handling
- **Integration Tests:** Performance testing with multiple transactions
- **Target Coverage:** 80%+

### v0.4-0.7 Testing
- Continue expanding Cucumber scenarios
- Add integration tests for new adapters
- Performance testing as data grows
- Target Coverage: 80%+

### v1.0 Testing
- **Complete Cucumber Coverage:** All 6 feature files fully implemented
- **Comprehensive Unit Tests:** All domain logic
- **Integration Tests:** All adapters
- **End-to-End Tests:** Critical user journeys
- **Performance Tests:** 500 positions stress test
- **Target Coverage:** 85%+

---

## Non-Functional Requirements by Version

### All Versions (v0.1+)
- **Code Quality:** Clean code, SOLID principles, DDD patterns
- **Architecture:** Hexagonal architecture enforced by architecture tests
- **Observability:** OTLP tracing
- **Database:** PostgreSQL with encryption at rest
- **Data Loss:** Zero tolerance (full ACID transactions)
- **Audit Trail:** All data modifications tracked

### v0.1-v1.0 (Local Deployment)
- **Deployment:** Docker Compose, localhost access
- **Authentication:** None (trusted local environment)
- **Encryption:** Database at rest, HTTP acceptable for localhost
- **Availability:** Not critical, acceptable downtime
- **Performance:** Reasonable response times (no strict SLA)

### v2.0+ (Cloud Deployment)
- **Deployment:** Terraform + Kubernetes
- **Authentication:** Required (mechanism TBD)
- **Encryption:** TLS/HTTPS mandatory, database at rest
- **Availability:** Higher uptime expectations
- **Backup:** Automated cloud backups
- **Monitoring:** Enhanced cloud monitoring

---

## Development Best Practices

### For Each Version
1. **Start with Cucumber Scenarios:** Define acceptance criteria first
2. **Implement One Vertical Slice:** Prove it works end-to-end
3. **Expand Horizontally:** Add related features
4. **Refactor:** Clean up as you learn
5. **Document:** Update ubiquitous language, ADRs
6. **Review:** Check architecture tests still pass
7. **Demo:** Verify working software before moving on

### Git Workflow
- **Main Branch:** Always deployable
- **Feature Branches:** For each user story
- **Commit Messages:** Reference scenarios/requirements
- **Tags:** Tag each version release

### Definition of Done (Each Version)
- ✅ All Cucumber scenarios for version pass
- ✅ Unit test coverage meets target
- ✅ Architecture tests pass
- ✅ Code reviewed (self-review sufficient)
- ✅ Documentation updated
- ✅ Application runs via Docker Compose
- ✅ OTLP traces visible
- ✅ Version tagged in git

---

## Risk Management

### Technical Risks
| Risk | Likelihood | Impact | Mitigation | Version |
|------|------------|--------|------------|---------|
| XIRR calculation complexity | Medium | High | Use proven library or defer to v0.3 | v0.3 |
| Broker format incompatibility | Medium | Medium | Start with simplest format | v0.2 |
| Performance with 500+ positions | Low | Medium | Performance tests, indexing strategy | v1.0 |
| Cloud migration complexity | Medium | High | Cloud-ready design from v0.1 | v2.0 |
| API rate limits for prices | Medium | Low | Caching, fallback to manual | v2.1 |

### Scope Risks
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Feature creep | High | High | Strict version boundaries, defer new ideas |
| Over-engineering MVP | Medium | Medium | Start with v0.1 minimal scope, resist adding features |
| Underestimating test effort | Medium | Medium | Testing is non-negotiable, adjust timeline not coverage |
| Polish trap (UI perfectionism) | Medium | Low | UI polish is lowest priority, functional is enough |

---

## Open Questions & Decisions Needed

### Before Starting v0.1
- [ ] **Java Version:** Confirm Java 17+ or 21?
- [ ] **Spring Boot Modules:** Which Spring Boot components to use?
- [ ] **XIRR Library:** Use library or implement from scratch?
- [ ] **UI Framework Decision:** Confirm Angular version
- [ ] **OTLP Collector:** Which local OTLP collector (Jaeger, Zipkin, other)?

### Before v0.2
- [ ] **First Broker Format:** Which of the 6 brokers has simplest export format?
- [ ] **Import Library:** Apache Commons CSV or other?

### Before v2.0
- [ ] **Cloud Provider:** AWS, Azure, GCP, or self-hosted Kubernetes?
- [ ] **Authentication Method:** OAuth2, basic auth, JWT, other?
- [ ] **Cloud Database:** Managed PostgreSQL or other?

---

## Success Metrics

### Learning Goals (Primary)
- ✅ Demonstrated understanding of DDD (aggregates, value objects, ubiquitous language)
- ✅ Implemented Hexagonal Architecture with clear boundaries
- ✅ Comprehensive BDD testing with Cucumber
- ✅ Clean, maintainable code following SOLID principles
- ✅ OTLP observability implemented correctly

### Functional Goals (Secondary)
- ✅ Application is usable for daily personal portfolio tracking
- ✅ Supports all 6 broker accounts
- ✅ Provides accurate performance metrics (including XIRR)
- ✅ Data accuracy verified through reconciliation

### Technical Goals
- ✅ Test coverage >80% by v1.0
- ✅ Application handles 500 positions efficiently
- ✅ Zero data loss
- ✅ Cloud migration successful (v2.0)

---

## Recommended Starting Point

### Immediate Next Steps

1. **Validate This Roadmap**
   - Review and approve version boundaries
   - Confirm v0.1 scope is acceptable
   - Resolve open technical questions

2. **Prepare for v0.1**
   - Set up development environment
   - Choose Java version and Spring Boot modules
   - Set up project structure (Maven/Gradle)
   - Configure PostgreSQL locally
   - Set up OTLP collector locally

3. **Start v0.1 Development**
   - Write first Cucumber scenario (add single position)
   - Implement domain model (Position, Instrument, Account)
   - Create repository adapter
   - Build basic UI
   - Iterate until v0.1 success criteria met

4. **Review and Proceed**
   - Demo v0.1 working software
   - Review lessons learned
   - Adjust v0.2 plan if needed
   - Continue to v0.2

---

## Appendix: Feature Mapping to Versions

### Portfolio Viewing Feature
- **v0.1:** Basic portfolio metrics (no XIRR, single account)
  - **Requirements:** FR-001 (partial), FR-002, FR-003, FR-004
- **v0.2:** Multi-account aggregation
  - **Requirements:** FR-005, FR-085, FR-093
- **v0.3:** Add XIRR
  - **Requirements:** FR-001 (complete), FR-006, FR-086, FR-087
- **v1.0:** Full feature including all scenarios
  - **Requirements:** All portfolio viewing requirements complete

### Position Details Feature
- **v0.1:** Basic position details (no XIRR)
  - **Requirements:** FR-011 (partial), FR-012, FR-014
- **v0.3:** Add XIRR
  - **Requirements:** FR-011 (complete), FR-015
- **v0.6:** Add bonds
  - **Requirements:** FR-013
- **v1.0:** Full feature including position list
  - **Requirements:** All position management requirements complete

### Data Import Feature
- **v0.1:** Not implemented (manual entry only)
  - **Requirements:** None
- **v0.2:** Generic import + one broker format
  - **Requirements:** FR-021, FR-022, FR-023, FR-024, FR-025, FR-026, FR-027, FR-028, FR-029, FR-030
- **v0.5:** All 6 broker formats
  - **Requirements:** FR-021 (extended to all formats)
- **v1.0:** Import history and duplicate prevention
  - **Requirements:** FR-031

### Manual Entry Feature
- **v0.1:** Stock/ETF entry only
  - **Requirements:** FR-041, FR-042, FR-044, FR-045, FR-046
- **v0.6:** Add Polish government bonds
  - **Requirements:** FR-043
- **v1.0:** Full validation and error handling
  - **Requirements:** All manual entry requirements complete

### Price Updates Feature
- **v0.1:** Hardcoded or minimal manual update
  - **Requirements:** None (basic support only)
- **v0.4:** Full manual and CSV price updates
  - **Requirements:** FR-051, FR-052, FR-053, FR-054, FR-055
- **v0.6:** Bond value updates
  - **Requirements:** FR-057
- **v1.0:** Price history
  - **Requirements:** FR-056
- **v2.1:** Automated API updates
  - **Requirements:** NFR-015 (automated)

### Reconciliation Feature
- **v0.1-v0.6:** Not implemented
  - **Requirements:** None
- **v0.7:** Full reconciliation feature
  - **Requirements:** FR-061, FR-062, FR-063, FR-064, FR-065
- **v1.0:** Reconciliation history and reporting
  - **Requirements:** FR-066

### Account Management Feature
- **v0.1:** Single account only
  - **Requirements:** None (implicit)
- **v0.2:** Multiple accounts
  - **Requirements:** FR-071, FR-072

### Metrics & Calculations
- **v0.1:** Basic calculations
  - **Requirements:** FR-081, FR-082, FR-083, FR-084, FR-089
- **v0.2:** Multi-account average cost
  - **Requirements:** FR-085
- **v0.3:** XIRR calculations
  - **Requirements:** FR-086, FR-087
- **Future (v2.0+):** Partial sales tracking
  - **Requirements:** FR-088

### System-Level Features
- **v0.1:** Core exclusions and tracking model
  - **Requirements:** FR-091 (stocks/ETFs), FR-092, FR-094, FR-095, FR-096
- **v0.2:** Aggregated view
  - **Requirements:** FR-093
- **v0.6:** Complete instrument support
  - **Requirements:** FR-091 (complete with bonds)

---

## Requirements Coverage Summary

### Total Requirements Overview

**Functional Requirements:** 57 total (from requirements/functional/functional-requirements.md)
- Must Have: 43
- Should Have: 12
- Could Have: 2

**Non-Functional Requirements:** 48 total (from requirements/non-functional/non-functional-requirements.md)
- Must Have: 37
- Should Have: 10
- Could Have: 3

**Grand Total:** 105 requirements

---

### Requirements by Version

| Version | New FRs | Cumulative FRs | New NFRs | Cumulative NFRs | Total Req | Priority Breakdown (MH/SH/CH) |
|---------|---------|----------------|----------|-----------------|-----------|-------------------------------|
| **v0.1** | 20 | 20 | 37 | 37 | 57 | FR: 18 MH, 0 SH, 0 CH / NFR: 37 MH, 0 SH, 0 CH |
| **v0.2** | 13 | 33 | 1 | 38 | 71 | FR: 31 MH, 2 SH, 0 CH / NFR: 37 MH, 1 SH, 0 CH |
| **v0.3** | 4 | 37 | 0 | 38 | 75 | FR: 35 MH, 2 SH, 0 CH / NFR: 37 MH, 1 SH, 0 CH |
| **v0.4** | 5 | 42 | 1 | 39 | 81 | FR: 40 MH, 2 SH, 0 CH / NFR: 37 MH, 2 SH, 0 CH |
| **v0.5** | 0 | 42 | 0 | 39 | 81 | FR: 40 MH, 2 SH, 0 CH / NFR: 37 MH, 2 SH, 0 CH |
| **v0.6** | 3 | 45 | 0 | 39 | 84 | FR: 40 MH, 5 SH, 0 CH / NFR: 37 MH, 2 SH, 0 CH |
| **v0.7** | 5 | 50 | 0 | 39 | 89 | FR: 40 MH, 10 SH, 0 CH / NFR: 37 MH, 2 SH, 0 CH |
| **v1.0** | 3 | 54* | 2 | 41 | 95 | FR: 40 MH, 12 SH, 1 CH / NFR: 37 MH, 4 SH, 0 CH |
| **v2.0** | 0 | 54 | 9 | 50 | 104 | FR: 40 MH, 12 SH, 1 CH / NFR: 37 MH, 13 SH, 0 CH |
| **v2.1** | 0 | 54 | 0 | 50 | 104 | FR: 40 MH, 12 SH, 1 CH / NFR: 37 MH, 13 SH, 0 CH |
| **Future** | 1+ | TBD | 3+ | TBD | TBD | Remaining: FR-088 (CH), NFR-043, NFR-044, NFR-047 (all CH) |

*Note: v1.0 implements 54 of 57 FRs. Remaining 3 FRs are: FR-088 (Could Have, future), and implicit support for FR-091 complete.

---

### Must Have Requirements Coverage

**Functional Requirements (43 total Must Have):**
- **v0.1:** 18 implemented (42% of Must Have FRs)
- **v0.2:** 31 implemented (72% of Must Have FRs)
- **v0.3:** 35 implemented (81% of Must Have FRs)
- **v0.4:** 40 implemented (93% of Must Have FRs)
- **v1.0:** 40 implemented (93% of Must Have FRs)

**Non-Functional Requirements (37 total Must Have):**
- **v0.1:** 37 implemented (100% of Must Have NFRs for local deployment)
- **v2.0:** All Must Have NFRs complete (local + cloud)

---

### Should Have Requirements Coverage

**Functional Requirements (12 total Should Have):**
- **v0.2:** 2 implemented (17%)
- **v0.6:** 5 implemented (42%)
- **v0.7:** 10 implemented (83%)
- **v1.0:** 12 implemented (100%)

**Non-Functional Requirements (10 total Should Have):**
- **v0.1:** 0 implemented (local deployment doesn't require cloud NFRs)
- **v0.4:** 2 implemented (20%)
- **v1.0:** 4 implemented (40%)
- **v2.0:** 13 implemented (100% + additional cloud requirements)

---

### Could Have Requirements Status

**Functional Requirements (2 total Could Have):**
- FR-066: Reconciliation History - **v1.0** ✅
- FR-088: Average Cost Method for Sales - **Future (v2.0+)**

**Non-Functional Requirements (3 total Could Have):**
- NFR-011: Availability for Personal Use - **v0.1** ✅ (implicit)
- NFR-043: Mobile Responsiveness - **Future (optional)**
- NFR-044: Accessibility Requirements - **Future (optional)**
- NFR-047: Export Capabilities - **Future (optional)**

---

### Functional Requirements by Category and Version

| Category | Total FRs | v0.1 | v0.2 | v0.3 | v0.4 | v0.5 | v0.6 | v0.7 | v1.0 |
|----------|-----------|------|------|------|------|------|------|------|------|
| Portfolio Management | 6 | 4 | 5 | 6 | 6 | 6 | 6 | 6 | 6 |
| Position Management | 5 | 2 | 2 | 4 | 4 | 4 | 5 | 5 | 5 |
| Data Import | 11 | 0 | 10 | 10 | 10 | 10 | 10 | 10 | 11 |
| Manual Entry | 6 | 5 | 5 | 5 | 5 | 5 | 6 | 6 | 6 |
| Price Management | 7 | 0 | 0 | 0 | 5 | 5 | 6 | 6 | 7 |
| Reconciliation | 6 | 0 | 0 | 0 | 0 | 0 | 0 | 5 | 6 |
| Account Management | 2 | 0 | 2 | 2 | 2 | 2 | 2 | 2 | 2 |
| Metrics & Calculations | 13 | 5 | 6 | 8 | 8 | 8 | 8 | 8 | 8 |
| System-Level | 6 | 4 | 5 | 5 | 5 | 5 | 6 | 6 | 6 |
| **Total Implemented** | **57** | **20** | **33** | **37** | **42** | **42** | **45** | **50** | **54** |
| **% Complete** | **100%** | **35%** | **58%** | **65%** | **74%** | **74%** | **79%** | **88%** | **95%** |

---

### Non-Functional Requirements by Category and Version

| Category | Total NFRs | v0.1 | v0.2 | v0.3 | v0.4 | v1.0 | v2.0 | v2.1 |
|----------|------------|------|------|------|------|------|------|------|
| Deployment & Infrastructure | 10 | 5 | 5 | 5 | 5 | 5 | 10 | 10 |
| Performance & Scalability | 6 | 4 | 4 | 4 | 5 | 6 | 6 | 6 |
| Security | 8 | 6 | 6 | 6 | 6 | 6 | 8 | 8 |
| Data Management | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 |
| Quality Attributes | 20 | 12 | 13 | 13 | 13 | 13 | 14 | 14 |
| Observability | 4 | 4 | 4 | 4 | 4 | 4 | 4 | 4 |
| Technology Stack | 8 | 6 | 6 | 6 | 6 | 6 | 8 | 8 |
| **Total Implemented** | **48** | **37** | **38** | **38** | **39** | **41** | **50** | **50** |
| **% Complete** | **100%** | **77%** | **79%** | **79%** | **81%** | **85%** | **100%** | **100%** |

---

### Version Milestones

**v0.1 - MVP (Domain Foundation):**
- Implements 35% of functional requirements
- Implements 77% of non-functional requirements (100% of Must Have NFRs for local)
- Focus: Architecture proof, single vertical slice
- **Key Achievement:** Clean architecture established

**v0.2 - Multi-Account Aggregation:**
- Implements 58% of functional requirements (+23%)
- Core business value proven (aggregation across accounts)
- **Key Achievement:** Main problem solved

**v0.3 - Performance Tracking:**
- Implements 65% of functional requirements (+7%)
- XIRR calculations complete
- **Key Achievement:** Full performance metrics

**v1.0 - Feature Complete (Local):**
- Implements 95% of functional requirements
- Implements 85% of non-functional requirements
- Ready for daily personal use (local deployment)
- **Key Achievement:** Production-ready local version

**v2.0 - Cloud Deployment:**
- Implements same 95% of functional requirements
- Implements 100% of non-functional requirements (includes all cloud NFRs)
- **Key Achievement:** Cloud-ready with authentication

---

### Requirements Not in Any Version (Deferred/Optional)

**Functional Requirements:**
- FR-088: Average Cost Method for Partial Sales (Could Have) - Future v2.0+

**Non-Functional Requirements:**
- NFR-043: Mobile Responsiveness (Could Have) - Optional future
- NFR-044: Accessibility Requirements (Could Have) - Optional future
- NFR-047: Export Capabilities (Could Have) - Optional future

**Total Deferred:** 4 requirements (all "Could Have" priority)

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-10 | Claude | Initial version based on requirements analysis |
| 1.1 | 2025-11-10 | Claude | Added comprehensive requirement ID traceability: Added "Requirements in Scope" and "Explicitly Out of Scope" sections for each version (v0.1-v2.1), Updated Version Comparison Matrix with requirements count row, Enhanced Appendix with requirement IDs for all features, Added complete Requirements Coverage Summary with detailed breakdowns by version, category, and priority |

---

**Next Steps:** Review this roadmap, approve v0.1 scope, and begin technical setup for MVP development.
