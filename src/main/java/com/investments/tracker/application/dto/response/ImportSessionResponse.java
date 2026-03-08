package com.investments.tracker.application.dto.response;

import java.time.LocalDateTime;

/** Response representing the state of an import session. */
public record ImportSessionResponse(
        String importSessionId,
        String status,
        String broker,
        String accountName,
        ImportSummaryDTO summary,
        LocalDateTime createdAt,
        LocalDateTime completedAt) {}
