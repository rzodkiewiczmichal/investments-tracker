package com.investments.tracker.application.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Request to confirm an import session with user-provided instrument mappings. */
public record ConfirmImportRequest(@NotNull @Valid List<MappingEntryRequest> mappings) {}
