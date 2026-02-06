package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountId;

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

    /**
     * Gets a specific account by ID.
     *
     * @param id the account ID
     * @return the account
     * @throws com.investments.tracker.application.exception.ResourceNotFoundException if account not found
     */
    Account getAccount(AccountId id);
}
