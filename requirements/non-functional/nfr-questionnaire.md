# Non-Functional Requirements - Questionnaire

Please answer the following questions to help define the non-functional requirements for the Investment Tracker application.

---

## 1. Deployment & Environment

### 1.1 Application Type
- [ ] Desktop application (runs locally on your machine)
- [X] Web application (accessed via browser)
- [ ] Cloud-hosted service
- [ ] Other: _________________

### 1.2 Target Operating System(s)
- [ ] macOS
- [ ] Windows
- [ ] Linux
- [ ] Cross-platform
- [X] Other: Web

### 1.3 Deployment Model
- **Where will this application run?**
  - [ ] Only on my personal computer
  - [ ] On multiple devices I own (laptop, desktop, etc.)
  - [ ] Shared with family/friends
  - [X] Other: Initially on my personal computer, but should be ready to be deployed in cloud. I want the most
  modern and clean tech stack for deployment - terraform? kubernetes? This is separate topic to discuss

### 1.4 Internet Connectivity
- **Does the application require internet connection to function?**
  - [ ] Must always be online
  - [ ] Can work offline (with some limitations)
  - [ ] Fully offline application
  - [X] Other: Online. Even in web mode high availability is not critical. 

---

## 2. Data Volume & Scale

### 2.1 Number of Accounts
- **How many broker accounts do you currently have?** up to 10
- **Expected growth in next 1-2 years?** up to 10

### 2.2 Number of Positions
- **How many positions (instruments) do you currently hold across all accounts?** up to 100
- **Expected maximum in next 1-2 years?** up to 500

### 2.3 Transaction History (if applicable in future)
- **Average number of transactions per month?** up to 100
- **How many years of history do you want to keep?** Full history, this is important requirement

### 2.4 Concurrent Users
- **Will only you use this application?** [X] Yes [ ] No
- **If no, how many users maximum?** N/A

---

## 3. Performance Expectations

### 3.1 Response Times
What is acceptable response time for:

- **Loading portfolio view (all positions)?**
  - [ ] < 1 second
  - [ ] < 3 seconds
  - [ ] < 5 seconds
  - [X] Don't care

- **Loading single position details?**
  - [ ] < 500ms
  - [ ] < 1 second
  - [ ] < 2 seconds
  - [X] Don't care

- **Importing CSV file (100 positions)?**
  - [ ] < 5 seconds
  - [ ] < 10 seconds
  - [ ] < 30 seconds
  - [X] Don't care

- **Updating prices for all positions?**
  - [ ] < 5 seconds
  - [ ] < 10 seconds
  - [ ] < 30 seconds
  - [X] Don't care

### 3.2 Data Refresh
- **How often do you expect to refresh prices?**
  - [ ] Real-time (continuously)
  - [ ] Every few minutes
  - [X] Once per day
  - [ ] Manual only
  - [ ] Other: _________________

---

## 4. Security Requirements

### 4.1 Authentication
- **Should the application require login?**
  - [ ] Yes, with username/password
  - [ ] Yes, with other method: _________________
  - [X] No authentication needed (single-user desktop app) - For version that runs on my local machine no auth needed. 
  For web/cloud version there will be for sure some auth mechanism but I want to discuss it later.

### 4.2 Data Sensitivity
- **How sensitive is your investment data?**
  - [X] Highly sensitive (requires encryption at rest and in transit)
  - [ ] Moderately sensitive (basic security measures)
  - [ ] Not particularly sensitive

### 4.3 Multi-User Access Control
- **If multi-user, do different users need different permissions?**
  - [X] N/A - single user only
  - [ ] Yes, need role-based access
  - [ ] No, all users have same access

### 4.4 Data Encryption
- **Should data be encrypted on disk?**
  - [X] Yes, all data encrypted - for simplicity let's say all data is sensitive - add a task in Github for the roadmask to define 
  different level of sensitivity
  - [ ] Yes, only sensitive data (account details, balances)
  - [ ] No encryption needed

---

## 5. Reliability & Availability

### 5.1 Uptime Requirements
- **How critical is 24/7 availability?**
  - [X] Not critical - desktop app used when needed
  - [ ] Important - should be accessible anytime
  - [ ] Critical - cannot tolerate downtime

### 5.2 Data Loss Tolerance
- **If data is lost, how serious is it?**
  - [X] Catastrophic - cannot recreate
  - [ ] Serious - would take hours to recreate
  - [ ] Annoying - would take 30-60 minutes to recreate
  - [ ] Minor - easy to re-import

### 5.3 Recovery Time Objective (RTO)
- **If system fails, how quickly must it be restored?**
  - [ ] Immediately
  - [ ] Within a few hours
  - [ ] Within a day
  - [X] Not critical

---

## 6. Data Persistence & Backup

### 6.1 Database Preference
- [ ] Embedded database (file-based, like SQLite/H2)
- [X] Server database (PostgreSQL, MySQL, etc.) 
- [ ] Cloud database
- [ ] No preference

### 6.2 Backup Strategy
- **How often should data be backed up?**
  - [ ] Real-time/automatic backup
  - [ ] Daily automatic backup
  - [ ] Manual backup when I choose
  - [X] No backup needed
  - [ ] Other: _________________

### 6.3 Backup Location
- **Where should backups be stored?**
  - [ ] Same machine (different folder)
  - [ ] External drive
  - [ ] Cloud storage (Dropbox, Google Drive, etc.)
  - [ ] Multiple locations
  - [X] Not applicable

### 6.4 Data Retention
- **How long should historical data be kept?**
  - [X] Forever
  - [ ] 5+ years
  - [ ] 1-3 years
  - [ ] Only current positions (no history)

---

## 7. Usability & User Experience

### 7.1 User Interface Type
- [X] Web-based UI (HTML/CSS/JavaScript)
- [ ] Desktop GUI (JavaFX, Swing)
- [ ] Command-line interface (CLI)
- [ ] REST API only (UI separate)
- [ ] No preference

### 7.2 User Experience Priority
- **Rate importance (1=not important, 5=critical):**
  - Modern, attractive UI: [ ] 1 [ ] 2 [ ] 3 [ ] 4 [X] 5
  - Simple, functional UI: [ ] 1 [ ] 2 [ ] 3 [ ] 4 [X] 5
  - Mobile-responsive: [X] 1 [ ] 2 [ ] 3 [ ] 4 [ ] 5
  - Accessibility (screen readers, etc.): [X] 1 [ ] 2 [ ] 3 [ ] 4 [ ] 5

### 7.3 Learning Curve
- **How much time is acceptable to learn the system?**
  - [ ] < 15 minutes (must be intuitive)
  - [ ] < 1 hour
  - [ ] < 1 day
  - [X] Don't care (it's just for me)

---

## 8. Integration & Extensibility

### 8.1 Price Data Sources
- **How will you obtain current prices?**
  - [ ] Manual entry only
  - [ ] API integration (which?: _________________)
  - [X] CSV file import with prices
  - [ ] Screen scraping from broker websites
  - [ ] Undecided

### 8.2 Export Capabilities
- **Do you need to export data?**
  - [ ] Yes, to CSV
  - [ ] Yes, to Excel
  - [ ] Yes, to PDF reports
  - [ ] Yes, via API
  - [X] No export needed 

### 8.3 Future Integrations
- **Any planned integrations?**
  - [ ] Tax software integration
  - [ ] Portfolio analytics tools
  - [ ] Broker APIs for automatic import
  - [X] None planned
  - [ ] Other: _________________

---

## 9. Monitoring & Observability

### 9.1 Logging
- **What level of logging do you need?**
  - [X] Detailed logs for troubleshooting - I want to monitor in OTLP standard. An OTLP server should be running on my local and ingesting traces
  - [ ] Basic error logging only
  - [ ] No logging needed

### 9.2 Error Handling
- **When errors occur, how should they be reported?**
  - [X] User-friendly error messages only
  - [ ] Detailed technical error messages
  - [ ] Error log file for later review
  - [ ] All of the above

### 9.3 Audit Trail
- **Do you need to track who changed what and when?**
  - [X] Yes, full audit trail
  - [ ] Basic change history
  - [ ] No audit trail needed

---

## 10. Maintainability & Development

### 10.1 Code Quality Priority
- **Rate importance (1=not important, 5=critical):**
  - Clean, maintainable code: [ ] 1 [ ] 2 [ ] 3 [ ] 4 [X] 5
  - Comprehensive test coverage: [ ] 1 [ ] 2 [ ] 3 [ ] 4 [X] 5
  - Documentation: [ ] 1 [ ] 2 [ ] 3 [ ] 4 [X] 5
  - Fast development speed: [ ] 1 [ ] 2 [X] 3 [ ] 4 [ ] 5

### 10.2 Technology Constraints
- **Are there any technology must-haves or must-not-haves?**
  - Must use: Native Java, as low as possible Spring or Spring Boot. Hexagonal architecture that will allow to distinguish modules using native Java from modules with Spring Boot. Frontend - Angular.
  - Must avoid: _________________
  - No constraints

### 10.3 Future Development
- **Who will maintain this application?**
  - [X] Only me
  - [ ] Potentially others in the future
  - [ ] Open source community
  - [ ] Undecided

---

## 11. Compliance & Legal

### 11.1 Data Privacy
- **Are there any data privacy regulations to comply with?**
  - [ ] GDPR (EU)
  - [ ] CCPA (California)
  - [ ] Other: _________________
  - [X] No compliance requirements
  - [ ] Not sure

### 11.2 Data Residency
- **Must data be stored in a specific geographic location?**
  - [ ] Yes: _________________
  - [X] No restrictions

---

## 12. Cost & Budget

### 12.1 Infrastructure Costs
- **What is your budget for infrastructure/hosting?**
  - [X] $0 (free/self-hosted only)
  - [ ] < $10/month
  - [ ] < $50/month
  - [ ] < $100/month
  - [ ] No budget constraint

### 12.2 Third-Party Services
- **Budget for external APIs or services (price data, etc.)?**
  - [X] $0 (free only)
  - [ ] < $20/month
  - [ ] < $50/month
  - [ ] Willing to pay for good service
  - [ ] Undecided

---

## 13. Additional Context

### 13.1 Priority Trade-offs
If you had to choose, what's most important?

**Rank these from 1 (most important) to 5 (least important):**
- [2] Fast development (get something working quickly)
- [1] Code quality (clean, maintainable, well-tested)
- [4] Performance (fast response times)
- [3] Features (rich functionality)
- [5] User experience (polished UI)

### 13.2 Timeline
- **When do you want MVP ready?**
  - [ ] ASAP
  - [ ] 1-2 months
  - [ ] 3-6 months
  - [ ] No rush
  - [X] Other: Does not matter

### 13.3 Other Considerations
**Any other requirements or constraints not covered above?**

1. I need to define clearly phases of the project: 
  * Phase 1: Basic functionalities, running on my local machine
  * Phase 2A: Basic functionalities, running in cloud
  * Phase 2B: Advanced functionalities, running on my local machine (alternative development path to 2A)
The version that is running on my local should be cloud ready, I want to build it from scratch with technology that will allow to ship it
    to cloud easily in the future.
A lot of non functional aspects will matter only in cloud version.

2. It's crucial to have in mind goals of this project and their priorities: 
  * Highest: Practice clean functionality documentation, clean architecture, clean code, automated tests
  * Medium: Deliver working software with basic functionalities
  * Lowest: Frontend in general
---

## Instructions

1. Fill out this questionnaire by checking boxes or providing answers
2. Save the file
3. Share with Claude to generate the formal non-functional-requirements.md document
4. Feel free to add "Not sure" or "TBD" for questions you're uncertain about
