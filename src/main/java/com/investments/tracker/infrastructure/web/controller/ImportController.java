package com.investments.tracker.infrastructure.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.investments.tracker.application.dto.mapper.ImportSessionMapper;
import com.investments.tracker.application.dto.request.ConfirmImportRequest;
import com.investments.tracker.application.dto.request.PriceOverrideRequest;
import com.investments.tracker.application.dto.response.ImportSessionResponse;
import com.investments.tracker.application.usecase.ConfirmImportUseCase;
import com.investments.tracker.application.usecase.GetImportSessionUseCase;
import com.investments.tracker.application.usecase.InitiateImportUseCase;
import com.investments.tracker.application.usecase.ProvideImportPricesUseCase;
import com.investments.tracker.domain.exception.ImportParsingException;
import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.InstrumentMapping;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ImportSessionId;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;

/** REST controller for broker transaction history import operations. */
@RestController
@RequestMapping("/api/v1/imports")
public class ImportController {

    private final InitiateImportUseCase initiateImportUseCase;
    private final ConfirmImportUseCase confirmImportUseCase;
    private final GetImportSessionUseCase getImportSessionUseCase;
    private final ProvideImportPricesUseCase provideImportPricesUseCase;
    private final ImportSessionMapper importSessionMapper;

    public ImportController(
            InitiateImportUseCase initiateImportUseCase,
            ConfirmImportUseCase confirmImportUseCase,
            GetImportSessionUseCase getImportSessionUseCase,
            ProvideImportPricesUseCase provideImportPricesUseCase,
            ImportSessionMapper importSessionMapper) {
        this.initiateImportUseCase = initiateImportUseCase;
        this.confirmImportUseCase = confirmImportUseCase;
        this.getImportSessionUseCase = getImportSessionUseCase;
        this.provideImportPricesUseCase = provideImportPricesUseCase;
        this.importSessionMapper = importSessionMapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImportSessionResponse initiateImport(
            @RequestParam("broker") String broker,
            @RequestParam("accountName") String accountName,
            @RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            ImportSession session =
                    initiateImportUseCase.initiateImport(
                            broker, AccountName.of(accountName), inputStream);
            return importSessionMapper.toResponse(session);
        } catch (IOException e) {
            throw new ImportParsingException("Failed to read uploaded file", e);
        }
    }

    @GetMapping("/{id}")
    public ImportSessionResponse getImportSession(@PathVariable UUID id) {
        ImportSession session = getImportSessionUseCase.getImportSession(ImportSessionId.of(id));
        return importSessionMapper.toResponse(session);
    }

    @PostMapping("/{id}/confirm")
    public ImportSessionResponse confirmImport(
            @PathVariable UUID id, @Valid @RequestBody ConfirmImportRequest request) {
        List<InstrumentMapping> mappings =
                request.mappings().stream()
                        .map(
                                entry ->
                                        InstrumentMapping.resolved(
                                                BrokerInstrumentName.of(entry.brokerName()),
                                                new InstrumentSymbol(entry.catalogSymbol())))
                        .toList();

        ImportSession session =
                confirmImportUseCase.confirmImport(ImportSessionId.of(id), mappings);
        return importSessionMapper.toResponse(session);
    }

    @PostMapping("/{id}/prices")
    public ImportSessionResponse providePrices(
            @PathVariable UUID id, @Valid @RequestBody PriceOverrideRequest request) {
        Map<InstrumentSymbol, Price> priceOverrides = new HashMap<>();
        for (PriceOverrideRequest.PriceEntryRequest entry : request.prices()) {
            InstrumentSymbol symbol = new InstrumentSymbol(entry.symbol());
            Currency currency = Currency.valueOf(entry.currency());
            Price price = new Price(new Money(new BigDecimal(entry.price()), currency));
            priceOverrides.put(symbol, price);
        }

        ImportSession session =
                provideImportPricesUseCase.providePrices(ImportSessionId.of(id), priceOverrides);
        return importSessionMapper.toResponse(session);
    }
}
