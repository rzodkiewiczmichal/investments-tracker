package com.investments.tracker.domain.exception;

import java.util.Set;

/**
 * Exception thrown when an import confirmation is attempted but not all unmatched instruments have
 * been mapped.
 */
public class IncompleteMappingsException extends DomainException {

    private final Set<String> unresolvedBrokerNames;

    public IncompleteMappingsException(String message, Set<String> unresolvedBrokerNames) {
        super(message);
        this.unresolvedBrokerNames = unresolvedBrokerNames;
    }

    public static IncompleteMappingsException of(Set<String> unresolvedBrokerNames) {
        return new IncompleteMappingsException(
                unresolvedBrokerNames.size()
                        + " instruments require mapping before import can be confirmed",
                unresolvedBrokerNames);
    }

    public Set<String> getUnresolvedBrokerNames() {
        return unresolvedBrokerNames;
    }
}
