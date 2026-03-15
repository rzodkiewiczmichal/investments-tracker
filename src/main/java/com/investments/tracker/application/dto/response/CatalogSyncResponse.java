package com.investments.tracker.application.dto.response;

/** Response DTO for instrument catalog synchronization results. */
public record CatalogSyncResponse(int created, int updated, int skipped) {}
