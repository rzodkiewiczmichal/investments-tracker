package com.investments.tracker.domain.repository;

import java.util.Optional;

import com.investments.tracker.domain.model.BrokerInstrumentMapping;
import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.BrokerName;

/** Repository for persistent broker-to-catalog instrument mappings. */
public interface BrokerInstrumentMappingRepository {

    /**
     * Finds a mapping for a specific broker and instrument name.
     *
     * @param broker the broker identifier
     * @param brokerInstrumentName the broker-specific instrument name
     * @return the mapping if it exists
     */
    Optional<BrokerInstrumentMapping> findMapping(
            BrokerName broker, BrokerInstrumentName brokerInstrumentName);

    /**
     * Saves a mapping, inserting or updating if one already exists for the same broker and
     * instrument name.
     *
     * @param mapping the mapping to save
     */
    void save(BrokerInstrumentMapping mapping);
}
