package com.investments.tracker.testutils;

import java.math.BigDecimal;

/**
 * Test data builders for domain objects.
 * <p>
 * Provides fluent builders and factory methods for creating test data.
 * Use these builders in unit tests and integration tests to create
 * consistent, valid test data.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * PositionData position = TestDataBuilder.position()
 *     .instrumentSymbol("MSFT")
 *     .quantity("50")
 *     .avgCostBasis("1200.00")
 *     .build();
 * </pre>
 * </p>
 *
 * @see <a href="../../docs/adr/ADR-013-mock-vs-real-dependencies.md">ADR-013: Mock vs Real Dependencies</a>
 */
public final class TestDataBuilder {

    private TestDataBuilder() {
        // Utility class
    }

    // --- Factory Methods ---

    /**
     * Creates a default account data builder.
     */
    public static AccountDataBuilder account() {
        return new AccountDataBuilder();
    }

    /**
     * Creates a default instrument data builder.
     */
    public static InstrumentDataBuilder instrument() {
        return new InstrumentDataBuilder();
    }

    /**
     * Creates a default position data builder.
     */
    public static PositionDataBuilder position() {
        return new PositionDataBuilder();
    }

    /**
     * Creates a default account holding data builder.
     */
    public static AccountHoldingDataBuilder accountHolding() {
        return new AccountHoldingDataBuilder();
    }

    // --- Builders ---

    /**
     * Builder for account test data.
     * Schema: accounts(id, name, broker_name, account_type, ...)
     */
    public static class AccountDataBuilder {
        private String name = "Test Account";
        private String brokerName = "Test Broker";
        private String accountType = "NORMAL";

        public AccountDataBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AccountDataBuilder brokerName(String broker) {
            this.brokerName = broker;
            return this;
        }

        public AccountDataBuilder normal() {
            this.accountType = "NORMAL";
            return this;
        }

        public AccountDataBuilder ike() {
            this.accountType = "IKE";
            return this;
        }

        public AccountDataBuilder ikze() {
            this.accountType = "IKZE";
            return this;
        }

        public AccountData build() {
            return new AccountData(name, brokerName, accountType);
        }
    }

    /**
     * Builder for instrument test data.
     * Schema: instruments(symbol, name, instrument_type, current_price_amount, ...)
     */
    public static class InstrumentDataBuilder {
        private String symbol = "AAPL";
        private String name = "Apple Inc.";
        private String instrumentType = "STOCK";
        private BigDecimal currentPrice = null;

        public InstrumentDataBuilder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public InstrumentDataBuilder name(String name) {
            this.name = name;
            return this;
        }

        public InstrumentDataBuilder stock() {
            this.instrumentType = "STOCK";
            return this;
        }

        public InstrumentDataBuilder etf() {
            this.instrumentType = "ETF";
            return this;
        }

        public InstrumentDataBuilder bondEtf() {
            this.instrumentType = "BOND_ETF";
            return this;
        }

        public InstrumentDataBuilder polishGovBond() {
            this.instrumentType = "POLISH_GOV_BOND";
            return this;
        }

        public InstrumentDataBuilder currentPrice(String price) {
            this.currentPrice = new BigDecimal(price);
            return this;
        }

        public InstrumentDataBuilder currentPrice(BigDecimal price) {
            this.currentPrice = price;
            return this;
        }

        public InstrumentData build() {
            return new InstrumentData(symbol, name, instrumentType, currentPrice);
        }
    }

    /**
     * Builder for position test data.
     * Schema: positions(instrument_symbol, total_quantity, avg_cost_basis_amount, ...)
     */
    public static class PositionDataBuilder {
        private String instrumentSymbol = "AAPL";
        private BigDecimal totalQuantity = new BigDecimal("100");
        private BigDecimal avgCostBasis = new BigDecimal("600.0000");

        public PositionDataBuilder instrumentSymbol(String symbol) {
            this.instrumentSymbol = symbol;
            return this;
        }

        public PositionDataBuilder quantity(String qty) {
            this.totalQuantity = new BigDecimal(qty);
            return this;
        }

        public PositionDataBuilder quantity(int qty) {
            this.totalQuantity = new BigDecimal(qty);
            return this;
        }

        public PositionDataBuilder avgCostBasis(String cost) {
            this.avgCostBasis = new BigDecimal(cost);
            return this;
        }

        public PositionDataBuilder avgCostBasis(int cost) {
            this.avgCostBasis = new BigDecimal(cost);
            return this;
        }

        public PositionData build() {
            return new PositionData(instrumentSymbol, totalQuantity, avgCostBasis);
        }
    }

    /**
     * Builder for account holding test data.
     * Schema: account_holdings(instrument_symbol, account_id, quantity, cost_basis_amount, ...)
     */
    public static class AccountHoldingDataBuilder {
        private String instrumentSymbol = "AAPL";
        private Long accountId = 1L;
        private BigDecimal quantity = new BigDecimal("100");
        private BigDecimal costBasis = new BigDecimal("600.0000");

        public AccountHoldingDataBuilder instrumentSymbol(String symbol) {
            this.instrumentSymbol = symbol;
            return this;
        }

        public AccountHoldingDataBuilder accountId(Long id) {
            this.accountId = id;
            return this;
        }

        public AccountHoldingDataBuilder quantity(String qty) {
            this.quantity = new BigDecimal(qty);
            return this;
        }

        public AccountHoldingDataBuilder quantity(int qty) {
            this.quantity = new BigDecimal(qty);
            return this;
        }

        public AccountHoldingDataBuilder costBasis(String cost) {
            this.costBasis = new BigDecimal(cost);
            return this;
        }

        public AccountHoldingDataBuilder costBasis(int cost) {
            this.costBasis = new BigDecimal(cost);
            return this;
        }

        public AccountHoldingData build() {
            return new AccountHoldingData(instrumentSymbol, accountId, quantity, costBasis);
        }
    }

    // --- Data Records ---

    /**
     * Data record for account test data.
     */
    public record AccountData(
            String name,
            String brokerName,
            String accountType
    ) {}

    /**
     * Data record for instrument test data.
     */
    public record InstrumentData(
            String symbol,
            String name,
            String instrumentType,
            BigDecimal currentPrice
    ) {}

    /**
     * Data record for position test data.
     */
    public record PositionData(
            String instrumentSymbol,
            BigDecimal totalQuantity,
            BigDecimal avgCostBasis
    ) {
        public BigDecimal investedAmount() {
            return totalQuantity.multiply(avgCostBasis);
        }

        public BigDecimal currentValue(BigDecimal currentPrice) {
            return totalQuantity.multiply(currentPrice);
        }

        public BigDecimal profitLoss(BigDecimal currentPrice) {
            return currentValue(currentPrice).subtract(investedAmount());
        }
    }

    /**
     * Data record for account holding test data.
     */
    public record AccountHoldingData(
            String instrumentSymbol,
            Long accountId,
            BigDecimal quantity,
            BigDecimal costBasis
    ) {
        public BigDecimal investedAmount() {
            return quantity.multiply(costBasis);
        }
    }
}
