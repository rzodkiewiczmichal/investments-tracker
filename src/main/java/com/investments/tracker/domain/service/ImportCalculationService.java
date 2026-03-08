package com.investments.tracker.domain.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Transaction;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.model.value.TransactionType;

/**
 * Domain service that computes account holdings from a list of transactions.
 *
 * <p>Uses weighted average cost method with commission included in cost basis. Pure domain logic —
 * no Spring dependencies.
 */
public class ImportCalculationService {

    /**
     * Computes holdings from transactions using weighted average cost method.
     *
     * <p>For each instrument:
     *
     * <ol>
     *   <li>BUY: accumulates total cost (qty * price + commission) and total quantity
     *   <li>SELL: subtracts quantity only (cost basis unchanged with average cost method)
     *   <li>Cost basis = total buy cost / total buy quantity
     *   <li>Net quantity = total buy qty - total sell qty
     * </ol>
     *
     * <p>Only instruments with positive net quantity are returned.
     *
     * @param transactions the list of resolved transactions
     * @param accountId the account to create holdings for
     * @return map of instrument symbol to computed account holding
     */
    public Map<InstrumentSymbol, AccountHolding> computeHoldings(
            List<Transaction> transactions, AccountId accountId) {
        Objects.requireNonNull(transactions, "transactions cannot be null");
        Objects.requireNonNull(accountId, "accountId cannot be null");

        // Accumulate per symbol: [0] = totalBuyCost (Money), [1] = totalBuyQty (BigDecimal), [2] =
        // totalSellQty (BigDecimal)
        Map<InstrumentSymbol, Object[]> accumulators = new HashMap<>();

        for (Transaction tx : transactions) {
            Object[] acc =
                    accumulators.computeIfAbsent(
                            tx.symbol(),
                            k -> new Object[] {Money.zero(), BigDecimal.ZERO, BigDecimal.ZERO});

            if (tx.type() == TransactionType.BUY) {
                acc[0] = ((Money) acc[0]).add(tx.totalCost());
                acc[1] = ((BigDecimal) acc[1]).add(tx.quantity().toBigDecimal());
            } else {
                acc[2] = ((BigDecimal) acc[2]).add(tx.quantity().toBigDecimal());
            }
        }

        Map<InstrumentSymbol, AccountHolding> holdings = new HashMap<>();
        for (var entry : accumulators.entrySet()) {
            Object[] acc = entry.getValue();
            Money totalBuyCost = (Money) acc[0];
            BigDecimal totalBuyQty = (BigDecimal) acc[1];
            BigDecimal totalSellQty = (BigDecimal) acc[2];
            BigDecimal netQty = totalBuyQty.subtract(totalSellQty);

            if (netQty.signum() > 0) {
                CostBasis costBasis = CostBasis.of(totalBuyCost.divide(totalBuyQty));
                holdings.put(
                        entry.getKey(),
                        new AccountHolding(accountId, Quantity.of(netQty), costBasis));
            }
        }

        return holdings;
    }
}
