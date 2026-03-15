package com.investments.tracker.domain.model.value;

import com.investments.tracker.domain.exception.DomainException;

/** Result of synchronizing the instrument catalog from an external source. */
public record CatalogSyncResult(int created, int updated, int skipped) {

    public CatalogSyncResult {
        if (created < 0) {
            throw new DomainException("created cannot be negative");
        }
        if (updated < 0) {
            throw new DomainException("updated cannot be negative");
        }
        if (skipped < 0) {
            throw new DomainException("skipped cannot be negative");
        }
    }
}
