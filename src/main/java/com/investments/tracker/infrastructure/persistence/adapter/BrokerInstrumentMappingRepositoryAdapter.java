package com.investments.tracker.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.investments.tracker.domain.model.BrokerInstrumentMapping;
import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.repository.BrokerInstrumentMappingRepository;

/** JdbcTemplate implementation for BrokerInstrumentMapping with composite primary key. */
@Repository
public class BrokerInstrumentMappingRepositoryAdapter implements BrokerInstrumentMappingRepository {

    private final JdbcTemplate jdbcTemplate;

    public BrokerInstrumentMappingRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<BrokerInstrumentMapping> findMapping(
            BrokerName broker, BrokerInstrumentName brokerInstrumentName) {
        var results =
                jdbcTemplate.query(
                        "SELECT broker, broker_instrument_name, catalog_symbol "
                                + "FROM broker_instrument_mappings "
                                + "WHERE broker = ? AND broker_instrument_name = ?",
                        (rs, rowNum) ->
                                new BrokerInstrumentMapping(
                                        BrokerName.of(rs.getString("broker")),
                                        BrokerInstrumentName.of(
                                                rs.getString("broker_instrument_name")),
                                        InstrumentSymbol.of(rs.getString("catalog_symbol"))),
                        broker.value(),
                        brokerInstrumentName.value());
        return results.stream().findFirst();
    }

    @Override
    public void save(BrokerInstrumentMapping mapping) {
        jdbcTemplate.update(
                "INSERT INTO broker_instrument_mappings (broker, broker_instrument_name, catalog_symbol) "
                        + "VALUES (?, ?, ?) "
                        + "ON CONFLICT (broker, broker_instrument_name) "
                        + "DO UPDATE SET catalog_symbol = EXCLUDED.catalog_symbol",
                mapping.broker().value(),
                mapping.brokerInstrumentName().value(),
                mapping.catalogSymbol().value());
    }
}
