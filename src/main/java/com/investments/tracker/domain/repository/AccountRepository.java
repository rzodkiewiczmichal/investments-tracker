package com.investments.tracker.domain.repository;

import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountId;

import java.util.Collection;
import java.util.Optional;

/**
 * Repository port for Account aggregate persistence.
 * <p>
 * This is a driven port that will be implemented by infrastructure adapters.
 * </p>
 */
public interface AccountRepository {

    /**
     * Finds an account by its ID.
     *
     * @param id the account ID
     * @return optional account
     */
    Optional<Account> findById(AccountId id);

    /**
     * Finds all accounts.
     *
     * @return collection of all accounts
     */
    Collection<Account> findAll();

    /**
     * Saves an account (insert or update).
     *
     * @param account the account to save
     * @return the saved account
     */
    Account save(Account account);

    /**
     * Deletes an account by its ID.
     *
     * @param id the account ID
     */
    void deleteById(AccountId id);

    /**
     * Checks if an account exists with the given ID.
     *
     * @param id the account ID
     * @return true if exists
     */
    boolean existsById(AccountId id);

    /**
     * Counts all accounts.
     *
     * @return the count
     */
    long count();
}
