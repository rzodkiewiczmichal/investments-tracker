package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.Account;

import java.util.Collection;

/**
 * Use case for querying accounts.
 */
public interface AccountQueryUseCase {

    /**
     * Lists all accounts.
     *
     * @return collection of accounts
     */
    Collection<Account> listAccounts();
}
