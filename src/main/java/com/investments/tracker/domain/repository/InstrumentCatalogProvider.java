package com.investments.tracker.domain.repository;

import java.util.Collection;

import com.investments.tracker.domain.model.Instrument;

/** Driven port for fetching instrument catalog data from an external source. */
public interface InstrumentCatalogProvider {

    /**
     * Fetches the US stock and ETF catalog from an external provider.
     *
     * @return collection of instruments with USD currency and mapped types
     */
    Collection<Instrument> fetchUsStockCatalog();
}
