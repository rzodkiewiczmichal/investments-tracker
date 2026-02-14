# Ubiquitous Language Dictionary
**Version:** 1.0
**Date:** 2025-09-16
**Domain:** Investment Tracking System

## Core Domain Terms

### Basic Entities

**Account**
- Definition: A brokerage account holding investments
- Note: Account type (IKE, IKZE, normal) is tracked but doesn't affect calculations
- Attributes: Account name, broker name, account identifier



**Instrument**
- Definition: A tradeable financial instrument (stock, ETF, or Polish government bond)
- Types: Stock, Stock ETF, Bond ETF, Polish Government Bond
- Attributes: Symbol/identifier, name, instrument type, currency, current price



### Aggregated Concepts

**Position**
- Definition: A holding of a specific instrument (stock, ETF, or bond) aggregated across all accounts
- Attributes: Instrument identifier, total quantity, average cost basis, current value
- Example: "100 shares of Apple stock with average cost of 600 PLN"



**Portfolio**
- Definition: The complete aggregated view of all positions across all accounts
- Attributes: Total current value, total invested amount, total return, XIRR
- Key Feature: Single unified view across multiple broker accounts



### Basic Financial Metrics

**Invested Amount**
- Definition: The total amount of money originally spent to acquire positions
- Based on: Average cost method for calculations
- Currency: PLN



**Current Value**
- Definition: The present market value of a position or portfolio
- Formula: Quantity × Current Price
- Currency: PLN



### Currency & Conversion

**Exchange Rate**
- Definition: The rate at which one currency can be converted to another
- Source: External provider (NBP API for official PLN rates)
- Usage: Applied at query time to convert native-currency values to PLN
- Example: ExchangeRate(GBP, PLN, 5.25) means 1 GBP = 5.25 PLN
- Identity: PLN-to-PLN exchange rate is always 1.0



### Calculated Metrics

**P&L (Profit & Loss)**
- Definition: The gain or loss on current positions
- Formula: Current Value - Invested Amount
- Display: Amount in PLN and percentage



**Total Return**
- Definition: The overall gain or loss on current holdings
- Formula: Current Value - Total Invested Amount
- Display: Amount in PLN and percentage



**XIRR (Extended Internal Rate of Return)**
- Definition: Annualized return rate considering timing of investments
- Purpose: Shows yearly performance percentage (e.g., 8% per year)
- Scope: Calculated for both portfolio and individual positions



### Operations

**Import**
- Definition: Process of loading position data into the system (one-time operation in MVP)
- Sources: Broker export files in various formats
- Purpose: Initial data population only (no updates in MVP)
- Required Fields:
  - **Instrument Identifier** (name, ticker, or ISIN)
  - **Quantity** (number of shares/units owned)
  - **Account Identifier** (to track which broker/account)
- Optional Fields (can be calculated or updated later):
  - **Average Cost** (if not provided, needs manual entry)
  - **Current Price** (can be updated separately)



**Transaction**
- Definition: A recorded buy action for an instrument
- Types: Buy only (sells not tracked - only current positions matter)
- Attributes: Date, instrument, quantity, price, account
- Note: Sell transactions and dividends excluded from scope



**Reconciliation**
- Definition: Process of verifying system data matches broker statements
- Purpose: Ensure data accuracy and completeness
- Scope: Position quantities and values



### Polish-Specific Terms

**IKE (Indywidualne Konto Emerytalne)**
- Definition: Individual Retirement Account in Polish system
- Treatment: Regular account for tracking purposes



**IKZE (Indywidualne Konto Zabezpieczenia Emerytalnego)**
- Definition: Individual Retirement Security Account in Polish system
- Treatment: Regular account for tracking purposes



**Polish Government Bonds**
- Definition: Government debt securities held to maturity
- Special Handling: Tracked for current value and P&L only
- Not Traded: Buy and hold investments



## Business Rules

1. **Aggregation Rule**: All positions in the same instrument across different accounts are combined into a single position view

2. **Cost Basis Rule**: Average cost method is used for all profit/loss calculations

3. **Currency Rule**: Aggregated values (invested amount, current value, P&L) are always displayed in PLN. Cost basis and current price are stored in their native currency and converted to PLN at query time using current exchange rates. Currency is an instrument-level property — all holdings of an instrument share its currency

4. **Pricing Rule**: End-of-day prices are sufficient for all calculations

5. **Scope Rule**: No tracking of dividends, fees, commissions, or complex corporate actions in MVP

## Out of Scope (Not Part of Domain)

- Dividend tracking and reinvestment
- Transaction fees and commissions
- Corporate actions (splits, mergers, spin-offs)
- Tax reporting and calculations
- Historical performance tracking
- Portfolio allocation analysis
- Risk metrics (volatility, beta, correlation)
- Benchmark comparisons
- Full multi-currency accounting (all values stored in multiple currencies simultaneously)
- Sector or geographic allocation tracking

---
*Note: This dictionary represents the MVP scope focused on aggregated portfolio viewing across multiple Polish brokerage accounts*