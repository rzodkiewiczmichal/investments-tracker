package com.investments.tracker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Transaction;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.Commission;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.model.value.TransactionType;

class ImportCalculationServiceTest {

    private final ImportCalculationService service = new ImportCalculationService();
    private final AccountId accountId = new AccountId(1L);

    @Test
    void buyOnlyProducesHoldingWithCostBasisIncludingCommission() {
        List<Transaction> transactions =
                List.of(tx("ATR", TransactionType.BUY, 100, "37.20", "13.06"));

        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(transactions, accountId);

        assertThat(holdings).hasSize(1);
        AccountHolding holding = holdings.get(new InstrumentSymbol("ATR"));
        assertThat(holding.quantity().toBigDecimal()).isEqualByComparingTo("100");
        // costBasis = (100 * 37.20 + 13.06) / 100 = 3733.06 / 100 = 37.3306
        assertThat(holding.costBasis().money().amount()).isEqualByComparingTo("37.3306");
    }

    @Test
    void buyAndPartialSellReducesQuantityButKeepsCostBasis() {
        List<Transaction> transactions =
                List.of(
                        tx("TOR", TransactionType.BUY, 163, "24.80", "15.77"),
                        tx("TOR", TransactionType.SELL, 50, "30.00", "5.00"));

        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(transactions, accountId);

        assertThat(holdings).hasSize(1);
        AccountHolding holding = holdings.get(new InstrumentSymbol("TOR"));
        assertThat(holding.quantity().toBigDecimal()).isEqualByComparingTo("113");
        // costBasis = (163 * 24.80 + 15.77) / 163 = 4058.17 / 163 ≈ 24.8967 (HALF_EVEN)
        assertThat(holding.costBasis().money().amount()).isEqualByComparingTo("24.8967");
    }

    @Test
    void completeSellOffProducesNoHolding() {
        List<Transaction> transactions =
                List.of(
                        tx("ALE", TransactionType.BUY, 100, "31.30", "12.21"),
                        tx("ALE", TransactionType.SELL, 100, "30.16", "11.76"));

        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(transactions, accountId);

        assertThat(holdings).isEmpty();
    }

    @Test
    void multipleInstrumentsProcessedIndependently() {
        List<Transaction> transactions =
                List.of(
                        tx("ATR", TransactionType.BUY, 10, "37.20", "1.00"),
                        tx("TOR", TransactionType.BUY, 20, "24.80", "2.00"),
                        tx("ATR", TransactionType.SELL, 5, "40.00", "1.00"));

        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(transactions, accountId);

        assertThat(holdings).hasSize(2);
        assertThat(holdings.get(new InstrumentSymbol("ATR")).quantity().toBigDecimal())
                .isEqualByComparingTo("5");
        assertThat(holdings.get(new InstrumentSymbol("TOR")).quantity().toBigDecimal())
                .isEqualByComparingTo("20");
    }

    @Test
    void multipleBuysAccumulateCostCorrectly() {
        List<Transaction> transactions =
                List.of(
                        tx("MBR", TransactionType.BUY, 100, "8.42", "3.00"),
                        tx("MBR", TransactionType.BUY, 200, "8.50", "6.00"));

        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(transactions, accountId);

        AccountHolding holding = holdings.get(new InstrumentSymbol("MBR"));
        assertThat(holding.quantity().toBigDecimal()).isEqualByComparingTo("300");
        // totalCost = (100*8.42+3.00) + (200*8.50+6.00) = 845.00 + 1706.00 = 2551.00
        // costBasis = 2551.00 / 300 = 8.5033
        assertThat(holding.costBasis().money().amount()).isEqualByComparingTo("8.5033");
    }

    @Test
    void emptyTransactionListProducesNoHoldings() {
        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(List.of(), accountId);

        assertThat(holdings).isEmpty();
    }

    private Transaction tx(
            String symbol, TransactionType type, int qty, String price, String commission) {
        return new Transaction(
                new InstrumentSymbol(symbol),
                type,
                Quantity.of(qty),
                Price.of(Money.pln(price)),
                Commission.pln(commission),
                Currency.PLN);
    }
}
