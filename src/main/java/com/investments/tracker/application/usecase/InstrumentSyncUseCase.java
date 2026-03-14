package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.value.CatalogSyncResult;

/** Use case for synchronizing the instrument catalog from an external source. */
public interface InstrumentSyncUseCase {

    /**
     * Synchronizes US instruments from an external catalog provider.
     *
     * @return sync result with created, updated, and skipped counts
     */
    CatalogSyncResult syncUsInstruments();
}
