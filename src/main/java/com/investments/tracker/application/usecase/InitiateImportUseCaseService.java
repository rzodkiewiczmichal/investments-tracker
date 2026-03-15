package com.investments.tracker.application.usecase;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investments.tracker.application.port.out.ParseResult;
import com.investments.tracker.application.port.out.TransactionHistoryParser;
import com.investments.tracker.domain.exception.DomainException;
import com.investments.tracker.domain.exception.ImportParsingException;
import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.InstrumentMapping;
import com.investments.tracker.domain.model.RawTransaction;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.domain.model.value.ImportSessionId;
import com.investments.tracker.domain.model.value.ImportSessionStatus;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.TransactionType;
import com.investments.tracker.domain.repository.BrokerInstrumentMappingRepository;
import com.investments.tracker.domain.repository.ImportSessionRepository;
import com.investments.tracker.domain.repository.InstrumentRepository;

/** Implementation of the initiate import use case. */
@Service
@Transactional
public class InitiateImportUseCaseService implements InitiateImportUseCase {

    private final List<TransactionHistoryParser> parsers;
    private final InstrumentRepository instrumentRepository;
    private final ImportSessionRepository importSessionRepository;
    private final BrokerInstrumentMappingRepository brokerMappingRepository;

    public InitiateImportUseCaseService(
            List<TransactionHistoryParser> parsers,
            InstrumentRepository instrumentRepository,
            ImportSessionRepository importSessionRepository,
            BrokerInstrumentMappingRepository brokerMappingRepository) {
        this.parsers = parsers;
        this.instrumentRepository = instrumentRepository;
        this.importSessionRepository = importSessionRepository;
        this.brokerMappingRepository = brokerMappingRepository;
    }

    @Override
    public ImportSession initiateImport(
            String brokerName, AccountName accountName, InputStream file) {
        Objects.requireNonNull(brokerName, "brokerName cannot be null");
        Objects.requireNonNull(accountName, "accountName cannot be null");
        Objects.requireNonNull(file, "file cannot be null");

        TransactionHistoryParser parser = findParser(brokerName);
        ParseResult parseResult = parser.parse(file);

        Map<BrokerInstrumentName, BigDecimal> netQuantities =
                computeNetQuantities(parseResult.transactions());
        List<InstrumentMapping> mappings =
                buildMappings(BrokerName.of(brokerName), netQuantities, parseResult.tickerHints());

        boolean allResolved = mappings.stream().allMatch(InstrumentMapping::isResolved);
        ImportSessionStatus status =
                allResolved
                        ? ImportSessionStatus.READY_TO_CONFIRM
                        : ImportSessionStatus.PENDING_REVIEW;

        ImportSession session =
                new ImportSession(
                        ImportSessionId.generate(),
                        status,
                        BrokerName.of(brokerName),
                        accountName,
                        parseResult.transactions(),
                        mappings,
                        LocalDateTime.now(),
                        null);

        return importSessionRepository.save(session);
    }

    private TransactionHistoryParser findParser(String brokerName) {
        return parsers.stream()
                .filter(p -> p.brokerName().equalsIgnoreCase(brokerName))
                .findFirst()
                .orElseThrow(() -> ImportParsingException.unsupportedBroker(brokerName));
    }

    private Map<BrokerInstrumentName, BigDecimal> computeNetQuantities(
            List<RawTransaction> transactions) {
        Map<BrokerInstrumentName, BigDecimal> quantities = new HashMap<>();
        for (RawTransaction tx : transactions) {
            quantities.merge(
                    tx.brokerInstrumentName(),
                    tx.type() == TransactionType.BUY
                            ? tx.quantity().toBigDecimal()
                            : tx.quantity().toBigDecimal().negate(),
                    BigDecimal::add);
        }
        return quantities;
    }

    private List<InstrumentMapping> buildMappings(
            BrokerName broker,
            Map<BrokerInstrumentName, BigDecimal> netQuantities,
            Map<BrokerInstrumentName, InstrumentSymbol> tickerHints) {
        List<InstrumentMapping> mappings = new ArrayList<>();

        for (var entry : netQuantities.entrySet()) {
            if (entry.getValue().signum() <= 0) {
                continue;
            }

            BrokerInstrumentName brokerInstrumentName = entry.getKey();

            // 1. Try parse broker name as InstrumentSymbol → check catalog
            try {
                InstrumentSymbol symbol = new InstrumentSymbol(brokerInstrumentName.value());
                if (instrumentRepository.findBySymbol(symbol).isPresent()) {
                    mappings.add(InstrumentMapping.resolved(brokerInstrumentName, symbol));
                    continue;
                }
            } catch (DomainException ignored) {
                // Broker name is not a valid symbol format
            }

            // 2. Look up persistent broker mapping from prior imports
            var persistedMapping =
                    brokerMappingRepository.findMapping(broker, brokerInstrumentName);
            if (persistedMapping.isPresent()) {
                mappings.add(
                        InstrumentMapping.resolved(
                                brokerInstrumentName, persistedMapping.get().catalogSymbol()));
                continue;
            }

            // 3. Check parser-provided ticker hints
            InstrumentSymbol hintSymbol = tickerHints.get(brokerInstrumentName);
            if (hintSymbol != null && instrumentRepository.findBySymbol(hintSymbol).isPresent()) {
                mappings.add(InstrumentMapping.resolved(brokerInstrumentName, hintSymbol));
                continue;
            }

            // 4. Unresolved — requires user mapping
            mappings.add(InstrumentMapping.unresolved(brokerInstrumentName));
        }

        return mappings;
    }
}
