# User Personas
**Version:** 1.0
**Date:** 2025-09-16
**Domain:** Investment Tracking System

## Primary Persona

### Michal - The Multi-Account Investor

**Role:** Individual Investor / System Owner

**Background:**
- Has investments spread across 6 different brokerage accounts
- Mix of regular brokerage accounts, Polish retirement accounts (IKE, IKZE), and government bonds account
- Currently tracks investments by logging into each broker separately
- Frustrated by not having a consolidated view of total portfolio performance

**Goals:**
- See total portfolio value across all accounts in one place
- Understand overall investment performance (am I making or losing money?)
- Know exactly how much I've invested vs. current value
- See performance metrics like XIRR to understand annual returns

**Pain Points:**
- "In one broker I'm +10%, in another -5%, in third +20% - I want to see total +8% or whatever it is"
- Time-consuming to log into multiple broker accounts
- Can't see the big picture of financial situation
- Difficult to make investment decisions without complete view

**Technical Comfort:**
- Willing to export CSV files from brokers
- Can do manual data entry if needed
- Comfortable with end-of-day data (no need for real-time)

**Key Behaviors:**
- Checks investments periodically (not daily)
- Makes buy decisions based on overall portfolio state
- Holds investments long-term (not day trading)
- Focuses on current holdings, not interested in historical tracking

**Success Criteria:**
- Can see total portfolio value in seconds, not minutes
- Trusts the numbers (reconciliation with broker data)
- Understands portfolio performance at a glance



## Secondary Personas

### System Administrator (Michal in technical role)

**Role:** System maintainer and data importer

**Goals:**
- Import data from various brokers efficiently
- Ensure data accuracy through reconciliation
- Keep position data up-to-date

**Key Behaviors:**
- Exports data from brokers (CSV or manual entry)
- Imports data into the system
- Verifies data matches broker statements
- Updates current prices

**Success Criteria:**
- Import process is straightforward
- Can identify and fix data discrepancies
- System correctly aggregates positions across accounts



## Out of Scope Personas

### Tax Accountant
- **Not Needed:** System doesn't track tax implications or generate tax reports

### Financial Advisor
- **Not Needed:** System is for personal use only, not for client management

### Day Trader
- **Not Supported:** System focuses on current positions, not frequent trading or historical analysis



## User Journey Summary

1. **Data Setup Phase:**
   - Export current positions from each broker
   - Import positions into system
   - Verify data accuracy

2. **Regular Usage Phase:**
   - Open system to view portfolio
   - See total value and performance
   - Review individual positions if needed
   - Make investment decisions based on complete picture

3. **Maintenance Phase:**
   - Periodically update position data
   - Update current prices (end-of-day)
   - Reconcile with broker statements