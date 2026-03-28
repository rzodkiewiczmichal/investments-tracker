package com.investments.tracker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("single buy produces holding with unit price as cost basis")
    void buyOnlyProducesHolding() {
        List<Transaction> transactions =
                List.of(tx("ATR", TransactionType.BUY, 100, "37.20", "13.06", day(1)));

        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(transactions, accountId);

        assertThat(holdings).hasSize(1);
        AccountHolding holding = holdings.get(new InstrumentSymbol("ATR"));
        assertThat(holding.quantity().toBigDecimal()).isEqualByComparingTo("100");
        // FIFO: single lot, cost basis = unit price (commission not included in FIFO lot cost)
        assertThat(holding.costBasis().money().amount()).isEqualByComparingTo("37.2000");
    }

    @Test
    @DisplayName("FIFO: sell consumes oldest lot, remaining has newest lot cost basis")
    void fifoSellConsumesOldestLot() {
        List<Transaction> transactions =
                List.of(
                        tx("TSLA", TransactionType.BUY, 1, "451.99", "0.00", day(1)),
                        tx("TSLA", TransactionType.BUY, 1, "388.76", "0.00", day(2)),
                        tx("TSLA", TransactionType.BUY, 1, "233.83", "0.00", day(3)),
                        tx("TSLA", TransactionType.SELL, 2, "440.27", "0.00", day(4)));

        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(transactions, accountId);

        assertThat(holdings).hasSize(1);
        AccountHolding holding = holdings.get(new InstrumentSymbol("TSLA"));
        assertThat(holding.quantity().toBigDecimal()).isEqualByComparingTo("1");
        // FIFO: sells consume lots at 451.99 and 388.76, remaining lot at 233.83
        assertThat(holding.costBasis().money().amount()).isEqualByComparingTo("233.8300");
    }

    @Test
    @DisplayName("FIFO: partial lot consumption leaves remainder at same price")
    void fifoPartialLotConsumption() {
        List<Transaction> transactions =
                List.of(
                        tx("MBR", TransactionType.BUY, 100, "8.42", "0.00", day(1)),
                        tx("MBR", TransactionType.BUY, 200, "8.50", "0.00", day(2)),
                        tx("MBR", TransactionType.SELL, 150, "10.00", "0.00", day(3)));

        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(transactions, accountId);

        AccountHolding holding = holdings.get(new InstrumentSymbol("MBR"));
        assertThat(holding.quantity().toBigDecimal()).isEqualByComparingTo("150");
        // FIFO: sell 150 consumes all 100 at 8.42 + 50 at 8.50, leaving 150 at 8.50
        assertThat(holding.costBasis().money().amount()).isEqualByComparingTo("8.5000");
    }

    @Test
    @DisplayName("complete sell-off produces no holding")
    void completeSellOffProducesNoHolding() {
        List<Transaction> transactions =
                List.of(
                        tx("ALE", TransactionType.BUY, 100, "31.30", "0.00", day(1)),
                        tx("ALE", TransactionType.SELL, 100, "30.16", "0.00", day(2)));

        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(transactions, accountId);

        assertThat(holdings).isEmpty();
    }

    @Test
    @DisplayName("multiple instruments processed independently")
    void multipleInstrumentsProcessedIndependently() {
        List<Transaction> transactions =
                List.of(
                        tx("ATR", TransactionType.BUY, 10, "37.20", "0.00", day(1)),
                        tx("TOR", TransactionType.BUY, 20, "24.80", "0.00", day(1)),
                        tx("ATR", TransactionType.SELL, 5, "40.00", "0.00", day(2)));

        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(transactions, accountId);

        assertThat(holdings).hasSize(2);
        assertThat(holdings.get(new InstrumentSymbol("ATR")).quantity().toBigDecimal())
                .isEqualByComparingTo("5");
        assertThat(holdings.get(new InstrumentSymbol("TOR")).quantity().toBigDecimal())
                .isEqualByComparingTo("20");
    }

    @Test
    @DisplayName(
            "multiple buys at different prices, FIFO cost basis is weighted average of remaining lots")
    void multipleBuysRemainingLotsCostBasis() {
        List<Transaction> transactions =
                List.of(
                        tx("MBR", TransactionType.BUY, 100, "8.42", "0.00", day(1)),
                        tx("MBR", TransactionType.BUY, 200, "8.50", "0.00", day(2)));

        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(transactions, accountId);

        AccountHolding holding = holdings.get(new InstrumentSymbol("MBR"));
        assertThat(holding.quantity().toBigDecimal()).isEqualByComparingTo("300");
        // Two lots: 100 @ 8.42 + 200 @ 8.50 => avg = (842+1700)/300 = 2542/300 = 8.4733
        assertThat(holding.costBasis().money().amount()).isEqualByComparingTo("8.4733");
    }

    @Test
    @DisplayName("empty transaction list produces no holdings")
    void emptyTransactionListProducesNoHoldings() {
        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(List.of(), accountId);

        assertThat(holdings).isEmpty();
    }

    @Test
    @DisplayName("transactions without dates are processed in list order")
    void transactionsWithoutDatesProcessedInOrder() {
        List<Transaction> transactions =
                List.of(
                        tx("X", TransactionType.BUY, 10, "100.00", "0.00", null),
                        tx("X", TransactionType.BUY, 10, "200.00", "0.00", null),
                        tx("X", TransactionType.SELL, 10, "150.00", "0.00", null));

        Map<InstrumentSymbol, AccountHolding> holdings =
                service.computeHoldings(transactions, accountId);

        AccountHolding holding = holdings.get(new InstrumentSymbol("X"));
        assertThat(holding.quantity().toBigDecimal()).isEqualByComparingTo("10");
        // Null dates sort first (nullsFirst), so all have equal ordering — list order preserved
        // First buy at 100 consumed by sell, remaining lot at 200
        assertThat(holding.costBasis().money().amount()).isEqualByComparingTo("200.0000");
    }

    private Transaction tx(
            String symbol,
            TransactionType type,
            int qty,
            String price,
            String commission,
            LocalDateTime date) {
        return new Transaction(
                new InstrumentSymbol(symbol),
                type,
                Quantity.of(qty),
                Price.of(Money.pln(price)),
                Commission.pln(commission),
                Currency.PLN,
                date);
    }

    private static LocalDateTime day(int day) {
        return LocalDateTime.of(2025, 1, day, 10, 0);
    }
}
