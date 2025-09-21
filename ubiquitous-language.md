# Ubiquitous Language Dictionary
**Version:** 1.0
**Date:** 2025-09-16
**Domain:** Investment Tracking System

## Core Domain Terms

### Investment Entities

**Position**
- Definition: A holding of a specific security (stock, ETF, or bond) aggregated across all accounts
- Attributes: Security identifier, total quantity, average cost basis, current value
- Example: "100 shares of Apple stock with average cost of $150"



**Security**
- Definition: A tradeable financial instrument (stock, ETF, or Polish government bond)
- Types: Stock, Stock ETF, Bond ETF, Polish Government Bond
- Attributes: Symbol/identifier, name, security type, current price



**Account**
- Definition: A brokerage account holding investments
- Note: Account type (IKE, IKZE, normal) is tracked but doesn't affect calculations
- Attributes: Account name, broker name, account identifier



**Portfolio**
- Definition: The complete aggregated view of all positions across all accounts
- Attributes: Total current value, total invested amount, total return, XIRR
- Key Feature: Single unified view across multiple broker accounts



### Financial Metrics

**Current Value**
- Definition: The present market value of a position or portfolio
- Formula: Quantity × Current Price
- Currency: PLN



**Invested Amount**
- Definition: The total amount of money originally spent to acquire positions
- Based on: Average cost method for calculations
- Currency: PLN



**Unrealized P&L (Profit & Loss)**
- Definition: The gain or loss on positions that haven't been sold yet
- Formula: Current Value - Invested Amount (for held positions)
- Display: Amount in PLN and percentage



**Realized P&L**
- Definition: The actual gain or loss from positions that have been sold
- Calculation Method: Average cost basis
- Display: Amount in PLN



**Total Return**
- Definition: The overall gain or loss including both realized and unrealized
- Formula: (Current Value + Realized P&L) - Total Invested Amount
- Display: Amount in PLN and percentage



**XIRR (Extended Internal Rate of Return)**
- Definition: Annualized return rate considering timing of investments
- Purpose: Shows yearly performance percentage (e.g., 8% per year)
- Scope: Calculated for both portfolio and individual positions



### Transactions & Activities

**Transaction**
- Definition: A recorded buy or sell action for a security
- Types: Buy, Sell
- Attributes: Date, security, quantity, price, account
- Note: Dividends excluded from MVP scope



**Import**
- Definition: Process of loading position or transaction data into the system
- Sources: Broker CSV files or manual entry
- Purpose: Initial data population and updates



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

1. **Aggregation Rule**: All positions in the same security across different accounts are combined into a single position view

2. **Cost Basis Rule**: Average cost method is used for all profit/loss calculations

3. **Currency Rule**: All values are displayed in PLN (Polish Zloty)

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
- Multiple currency support (future consideration)
- Sector or geographic allocation tracking

---
*Note: This dictionary represents the MVP scope focused on aggregated portfolio viewing across multiple Polish brokerage accounts*