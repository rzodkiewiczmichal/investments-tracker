package com.investments.tracker.application.usecase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.CatalogSyncResult;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.repository.InstrumentCatalogProvider;
import com.investments.tracker.domain.repository.InstrumentRepository;

/** Synchronizes the instrument catalog from an external provider into the local repository. */
@Service
@Transactional
public class InstrumentSyncUseCaseService implements InstrumentSyncUseCase {

    private final InstrumentCatalogProvider catalogProvider;
    private final InstrumentRepository instrumentRepository;

    public InstrumentSyncUseCaseService(
            InstrumentCatalogProvider catalogProvider, InstrumentRepository instrumentRepository) {
        this.catalogProvider = catalogProvider;
        this.instrumentRepository = instrumentRepository;
    }

    @Override
    public CatalogSyncResult syncUsInstruments() {
        Collection<Instrument> fetchedInstruments = catalogProvider.fetchUsStockCatalog();

        Map<InstrumentSymbol, Instrument> existingBySymbol =
                instrumentRepository.findAll().stream()
                        .collect(Collectors.toMap(Instrument::symbol, Function.identity()));

        int created = 0;
        int updated = 0;
        int skipped = 0;
        List<Instrument> toSave = new ArrayList<>();

        for (Instrument fetched : fetchedInstruments) {
            Instrument existing = existingBySymbol.get(fetched.symbol());

            if (existing == null) {
                toSave.add(fetched);
                created++;
            } else if (existing.currency() == Currency.USD) {
                toSave.add(fetched);
                updated++;
            } else {
                skipped++;
            }
        }

        instrumentRepository.saveAll(toSave);

        return new CatalogSyncResult(created, updated, skipped);
    }
}
