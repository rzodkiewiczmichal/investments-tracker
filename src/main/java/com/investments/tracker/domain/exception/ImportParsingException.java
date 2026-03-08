package com.investments.tracker.domain.exception;

/** Exception thrown when a broker import file cannot be parsed. */
public class ImportParsingException extends DomainException {

    public ImportParsingException(String message) {
        super(message);
    }

    public ImportParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ImportParsingException invalidFormat(int lineNumber, String detail) {
        return new ImportParsingException("Invalid format at line " + lineNumber + ": " + detail);
    }

    public static ImportParsingException unsupportedBroker(String brokerName) {
        return new ImportParsingException("No parser available for broker: " + brokerName);
    }

    public static ImportParsingException emptyFile() {
        return new ImportParsingException("Import file contains no transaction data");
    }
}
