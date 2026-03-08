package com.investments.tracker.domain.exception;

import java.util.UUID;

/** Exception thrown when an import session cannot be found. */
public class ImportSessionNotFoundException extends DomainException {

    public ImportSessionNotFoundException(String message) {
        super(message);
    }

    public static ImportSessionNotFoundException byId(UUID id) {
        return new ImportSessionNotFoundException("Import session not found: " + id);
    }
}
