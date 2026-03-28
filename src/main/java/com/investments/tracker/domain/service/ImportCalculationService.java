package com.investments.tracker.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Transaction;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.model.value.TransactionType;

/**
 * Domain service that computes account holdings from a list of transactions.
 *
 * <p>Uses FIFO (first-in, first-out) cost method: sells consume the oldest buy lots first. The cost
 * basis of remaining shares reflects only the lots that have not been sold. Pure domain logic — no
 * Spring dependencies.
 */
public class ImportCalculationService {

    private static final int COST_BASIS_SCALE = 4;

    /**
     * Computes holdings from transactions using FIFO cost method.
     *
     * <p>For each instrument, transactions are sorted chronologically. Buy transactions create lots
     * (quantity + unit cost). Sell transactions consume lots starting from the oldest. The cost
     * basis of remaining shares is the weighted average of unsold lots.
     *
     * <p>Only instruments with positive remaining quantity are returned.
     *
     * @param transactions the list of resolved transactions
     * @param accountId the account to create holdings for
     * @return map of instrument symbol to computed account holding
     */
    public Map<InstrumentSymbol, AccountHolding> computeHoldings(
            List<Transaction> transactions, AccountId accountId) {
        Objects.requireNonNull(transactions, "transactions cannot be null");
        Objects.requireNonNull(accountId, "accountId cannot be null");

        Map<InstrumentSymbol, List<Transaction>> bySymbol = groupBySymbol(transactions);
        Map<InstrumentSymbol, AccountHolding> holdings = new HashMap<>();

        for (var entry : bySymbol.entrySet()) {
            List<Transaction> sorted = sortChronologically(entry.getValue());
            LinkedList<BuyLot> lots = applyFifo(sorted);

            if (!lots.isEmpty()) {
                BigDecimal totalQty = BigDecimal.ZERO;
                Money totalCost = Money.zero(lots.getFirst().currency);

                for (BuyLot lot : lots) {
                    totalQty = totalQty.add(lot.remainingQty);
                    totalCost = totalCost.add(lot.unitCost.multiply(lot.remainingQty));
                }

                if (totalQty.signum() > 0) {
                    BigDecimal avgCost =
                            totalCost
                                    .amount()
                                    .divide(totalQty, COST_BASIS_SCALE, RoundingMode.HALF_UP);
                    CostBasis costBasis =
                            CostBasis.of(new Money(avgCost, lots.getFirst().currency));
                    holdings.put(
                            entry.getKey(),
                            new AccountHolding(accountId, Quantity.of(totalQty), costBasis));
                }
            }
        }

        return holdings;
    }

    private Map<InstrumentSymbol, List<Transaction>> groupBySymbol(List<Transaction> transactions) {
        Map<InstrumentSymbol, List<Transaction>> map = new HashMap<>();
        for (Transaction tx : transactions) {
            map.computeIfAbsent(tx.symbol(), k -> new ArrayList<>()).add(tx);
        }
        return map;
    }

    private List<Transaction> sortChronologically(List<Transaction> transactions) {
        return transactions.stream()
                .sorted(
                        Comparator.comparing(
                                Transaction::transactionDate,
                                Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();
    }

    private LinkedList<BuyLot> applyFifo(List<Transaction> sorted) {
        LinkedList<BuyLot> lots = new LinkedList<>();

        for (Transaction tx : sorted) {
            if (tx.type() == TransactionType.BUY) {
                Money unitCost = tx.unitPrice().money();
                lots.addLast(new BuyLot(tx.quantity().toBigDecimal(), unitCost, tx.currency()));
            } else {
                BigDecimal remaining = tx.quantity().toBigDecimal();
                while (remaining.signum() > 0 && !lots.isEmpty()) {
                    BuyLot oldest = lots.getFirst();
                    int cmp = oldest.remainingQty.compareTo(remaining);
                    if (cmp <= 0) {
                        remaining = remaining.subtract(oldest.remainingQty);
                        lots.removeFirst();
                    } else {
                        oldest.remainingQty = oldest.remainingQty.subtract(remaining);
                        remaining = BigDecimal.ZERO;
                    }
                }
            }
        }

        return lots;
    }

    private static class BuyLot {
        BigDecimal remainingQty;
        final Money unitCost;
        final Currency currency;

        BuyLot(BigDecimal qty, Money unitCost, Currency currency) {
            this.remainingQty = qty;
            this.unitCost = unitCost;
            this.currency = currency;
        }
    }
}
