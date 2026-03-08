package com.investments.tracker.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.domain.model.value.ImportSessionId;
import com.investments.tracker.domain.model.value.ImportSessionStatus;

/**
 * Aggregate representing an in-progress or completed import session.
 *
 * <p>Stores parsed raw transactions and instrument mappings so the original file does not need to
 * be re-parsed when the user confirms the import.
 */
public record ImportSession(
        ImportSessionId id,
        ImportSessionStatus status,
        BrokerName broker,
        AccountName accountName,
        List<RawTransaction> transactions,
        List<InstrumentMapping> instrumentMappings,
        LocalDateTime createdAt,
        LocalDateTime completedAt) {

    public ImportSession {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(status, "status cannot be null");
        Objects.requireNonNull(broker, "broker cannot be null");
        Objects.requireNonNull(accountName, "accountName cannot be null");
        Objects.requireNonNull(transactions, "transactions cannot be null");
        Objects.requireNonNull(instrumentMappings, "instrumentMappings cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
        transactions = List.copyOf(transactions);
        instrumentMappings = List.copyOf(instrumentMappings);
    }

    public List<InstrumentMapping> unresolvedMappings() {
        return instrumentMappings.stream().filter(InstrumentMapping::isUnresolved).toList();
    }

    public boolean isReadyToConfirm() {
        return unresolvedMappings().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImportSession other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
