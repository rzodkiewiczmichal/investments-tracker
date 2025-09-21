# Functional Requirements Interview
**Version:** 2.0 (Second Iteration)
**Date:** 2025-09-16
**Previous Version:** 1.0
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

**A:** Only buy, sell, dividends (Note: Later clarified - only tracking current positions, not historical sells)



**Q:** Do you track corporate actions? (splits, mergers, spin-offs)

**A:** No, not part of the domain



**Q:** How do you handle dividend reinvestments?

**A:** No, not part of the domain



## Performance & Analytics

### 4. Tracking Metrics

**Q:** What performance metrics are most important? (total return, XIRR, unrealized P&L)

**A:** I don't understand this question, share simple words definition of each.




**Clarification:**
- **Total Return:** How much money you made/lost in total (current value + dividends - invested amount)
- **XIRR:** Your annualized return considering when you invested (e.g., 8% per year)
- **Unrealized P&L:** Profit/loss on investments you still own (haven't sold yet)


**Follow-up Q:** Based on these definitions, which metrics would you like to track?
**A**: Total Return, XIRR, Unrealized P&L - all are needed 


**Q:** Do you need cost basis tracking? (FIFO, LIFO, average cost)

**A:** I don't understand this question, share simple words definition of each.



**Clarification:**
- **Cost Basis:** The original price you paid for an investment (used for calculating profit/loss)
- **FIFO (First In, First Out):** When selling, assumes you sell oldest shares first
- **LIFO (Last In, First Out):** When selling, assumes you sell newest shares first
- **Average Cost:** Uses average price of all your purchases

**Follow-up Q:** Which method should the system use when calculating profits from partial sales?
**A:** Average Cost



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



**Clarification:** This means comparing your system's data with broker statements to ensure everything matches (no missing transactions, correct balances)

**Follow-up Q:** Do you need the ability to verify that your system's portfolio matches what brokers show?

**A:** Yes, this would be useful to ensure data accuracy



**Q:** Export formats needed?

**A:** No export needed

---

## Additional Questions for Second Iteration

### 8. Portfolio Views & Information

**Q:** What information do you want to see about your overall portfolio?

**A:** Current value, invested amount, profit/loss (realized and unrealized), total return, XIRR. Let's forget dividens



**Q:** What information do you want to see about individual positions (specific stock/ETF holdings)?

**A:** Current value, invested amount, profit/loss (realized and unrealized), total return, XIRR. Let's forget dividens



**Q:** Do you need to see aggregated view across all accounts or per-account views?

**A:** Only aggregated view across all accounts - this is the main point of my system, being able to see everything in one place


### 9. Transaction Management

**Q:** When you buy/sell the same stock multiple times, do you need to track each purchase separately (lots) or just the total position?

**A:** Just the total position, no need to track individual lots. Not even sure this is needed, MVP of my system is: import all my owned positions from brokers, show me total



**Q:** Do you need to track instruments you sold and no longer own?

**A:** No, I only care about what I currently own. No need to track historical positions or realized gains from past sales



**Q:** How do you want to handle Polish government bonds - are they traded like stocks or held to maturity?

**A:** They are held to maturity, I just want to see current value and profit/loss



**Q:** Do you need to track transaction fees/commissions?

**A:** No, not part of the domain



### 10. Business Goals & Constraints

**Q:** What is the main problem you're trying to solve with this system?

**A:** I want to see all my investments in one place, so I can understand my total financial situation. My main problem is: in one broker I'm +10%, in another -5%, in third +20% - I want to see total +8% or whatever it is



**Q:** What do you currently use to track investments and what's wrong with it?

**A:** I don't use anything, I just log in to each broker separately.


**Q:** Are there any specific Polish regulations or requirements we should consider?

**A:** No, not part of the domain. System is just for me, not for public use