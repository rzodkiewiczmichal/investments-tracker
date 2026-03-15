package com.investments.tracker.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.investments.tracker.domain.model.BrokerInstrumentMapping;
import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.infrastructure.IntegrationTestBase;
import com.investments.tracker.testutils.IntegrationTestHelper;

@DisplayName("BrokerInstrumentMappingRepositoryAdapter")
class BrokerInstrumentMappingRepositoryAdapterTest extends IntegrationTestBase {

    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private BrokerInstrumentMappingRepositoryAdapter adapter;

    private IntegrationTestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new IntegrationTestHelper(jdbcTemplate);
        helper.cleanDatabase();
        jdbcTemplate.update(
                "INSERT INTO instruments (symbol, name, instrument_type, currency, market, version) VALUES (?, ?, ?, ?, ?, 0)",
                "MSFT.US",
                "Microsoft",
                "STOCK",
                "USD",
                "US");
        jdbcTemplate.update(
                "INSERT INTO instruments (symbol, name, instrument_type, currency, market, version) VALUES (?, ?, ?, ?, ?, 0)",
                "AAPL.US",
                "Apple",
                "STOCK",
                "USD",
                "US");
    }

    @Test
    @DisplayName("should save and find mapping")
    void shouldSaveAndFindMapping() {
        BrokerInstrumentMapping mapping =
                new BrokerInstrumentMapping(
                        BrokerName.of("XTB"),
                        BrokerInstrumentName.of("Microsoft"),
                        InstrumentSymbol.of("MSFT.US"));

        adapter.save(mapping);

        Optional<BrokerInstrumentMapping> found =
                adapter.findMapping(BrokerName.of("XTB"), BrokerInstrumentName.of("Microsoft"));
        assertThat(found).isPresent();
        assertThat(found.get().catalogSymbol().value()).isEqualTo("MSFT.US");
    }

    @Test
    @DisplayName("should return empty for non-existent mapping")
    void shouldReturnEmptyForNonExistent() {
        Optional<BrokerInstrumentMapping> found =
                adapter.findMapping(BrokerName.of("XTB"), BrokerInstrumentName.of("Unknown"));
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should upsert on conflict")
    void shouldUpsertOnConflict() {
        BrokerInstrumentMapping original =
                new BrokerInstrumentMapping(
                        BrokerName.of("XTB"),
                        BrokerInstrumentName.of("Microsoft"),
                        InstrumentSymbol.of("MSFT.US"));
        adapter.save(original);

        BrokerInstrumentMapping updated =
                new BrokerInstrumentMapping(
                        BrokerName.of("XTB"),
                        BrokerInstrumentName.of("Microsoft"),
                        InstrumentSymbol.of("AAPL.US"));
        adapter.save(updated);

        Optional<BrokerInstrumentMapping> found =
                adapter.findMapping(BrokerName.of("XTB"), BrokerInstrumentName.of("Microsoft"));
        assertThat(found).isPresent();
        assertThat(found.get().catalogSymbol().value()).isEqualTo("AAPL.US");
    }

    @Test
    @DisplayName("should distinguish mappings by broker")
    void shouldDistinguishByBroker() {
        adapter.save(
                new BrokerInstrumentMapping(
                        BrokerName.of("XTB"),
                        BrokerInstrumentName.of("Microsoft"),
                        InstrumentSymbol.of("MSFT.US")));
        adapter.save(
                new BrokerInstrumentMapping(
                        BrokerName.of("mBank"),
                        BrokerInstrumentName.of("Microsoft"),
                        InstrumentSymbol.of("AAPL.US")));

        Optional<BrokerInstrumentMapping> xtb =
                adapter.findMapping(BrokerName.of("XTB"), BrokerInstrumentName.of("Microsoft"));
        Optional<BrokerInstrumentMapping> mbank =
                adapter.findMapping(BrokerName.of("mBank"), BrokerInstrumentName.of("Microsoft"));

        assertThat(xtb.get().catalogSymbol().value()).isEqualTo("MSFT.US");
        assertThat(mbank.get().catalogSymbol().value()).isEqualTo("AAPL.US");
    }
}
