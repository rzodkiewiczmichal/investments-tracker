# Functional Requirements Interview
**Version:** 1.0 (First Iteration)
**Date:** 2025-09-16
**Interviewer:** Business Analyst
**Stakeholder:** Product Owner

## Core Domain Understanding

### 1. Investment Types & Products

**Q:** What types of investments do you track? (stocks, ETFs, bonds, crypto, mutual funds, options, etc.)
**A:** Stocks, ETFs, Polish government bonds. ETFs are stock ETFs and bond ETFs.

**Q:** Do you differentiate between asset classes (equities, fixed income, commodities, real estate)?
**A:** No

**Q:** Are derivatives or complex instruments in scope?
**A:** No, I think all above are simple instruments.

### 2. Broker Account Structure

**Q:** How many broker accounts do you typically manage?
**A:** I already have 6 accounts, no changes planned

**Q:** Are these accounts of different types? (cash, margin, retirement/IRA, taxable)
**A:** There are different types: normal brokers plus Polish account (IKE) and retirement account (IKZE) plus Polish government bonds account is specific - I don't see a reason for this type to matter. My system should just import data from each account

**Q:** Do you need to track accounts in different currencies?
**A:** I don't know yet, let's say all in PLN for now. This will need some extra focus in later stage

### 3. Key Investment Activities

**Q:** What are the main actions you perform? (buy, sell, dividends, transfers between accounts)
**A:** Only buy, sell, dividends

**Q:** Do you track corporate actions? (splits, mergers, spin-offs)
**A:** No, not part of the domain

**Q:** How do you handle dividend reinvestments?
**A:** No, not part of the domain

## Performance & Analytics

### 4. Tracking Metrics

**Q:** What performance metrics are most important? (total return, XIRR, unrealized P&L)
**A:** I don't understand this question, share simple words definition of each.

**Q:** Do you need cost basis tracking? (FIFO, LIFO, average cost)
**A:** I don't understand this question, share simple words definition of each.

**Q:** Time horizons for performance? (daily, monthly, yearly, since inception)
**A:** I just want to see current state, no history needed

### 5. Portfolio Analysis

**Q:** Do you track allocation by sector, geography, asset class?
**A:** No, not part of the domain in MVP

**Q:** Risk metrics needed? (volatility, correlation, beta)
**A:** No, not part of the domain

**Q:** Benchmark comparisons? (S&P 500, other indices)
**A:** No, not part of the domain

## Data & Integration

### 6. Data Sources

**Q:** How do you currently get transaction data? (manual entry, broker CSV/API exports)
**A:** I don't know yet, base plan is to export everything from brokers as something like CSV and import to my system. In worst case I will do manual entry.

**Q:** Real-time vs end-of-day pricing needs?
**A:** End-of-day is enough

**Q:** Historical price data requirements?
**A:** Not needed

### 7. Tax & Reporting

**Q:** Tax reporting needs? (capital gains, wash sales, tax lots)
**A:** Not part of the domain

**Q:** Statement reconciliation with brokers?
**A:** I don't understand the question

**Q:** Export formats needed?
**A:** No export needed