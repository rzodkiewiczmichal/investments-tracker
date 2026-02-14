package com.investments.tracker.domain.exception;

import com.investments.tracker.domain.model.value.AccountId;

/** Exception thrown when an account cannot be found. */
public class AccountNotFoundException extends DomainException {

    private final AccountId accountId;

    public AccountNotFoundException(AccountId accountId) {
        super("Account not found with ID: " + accountId.value());
        this.accountId = accountId;
    }

    public AccountId getAccountId() {
        return accountId;
    }
}
