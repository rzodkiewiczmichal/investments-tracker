package com.investments.tracker.domain.model.value;

/**
 * Lifecycle status of an import session.
 *
 * <p>{@code PENDING_REVIEW} — some instruments are unmatched, user must provide mappings.
 *
 * <p>{@code READY_TO_CONFIRM} — all instruments matched, awaiting user confirmation.
 *
 * <p>{@code COMPLETED} — import has been confirmed and positions created.
 */
public enum ImportSessionStatus {
    PENDING_REVIEW,
    READY_TO_CONFIRM,
    COMPLETED
}
