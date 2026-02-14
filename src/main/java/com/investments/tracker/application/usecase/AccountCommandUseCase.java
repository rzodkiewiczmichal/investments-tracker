package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerName;

/** Use case for account commands (create). */
public interface AccountCommandUseCase {

    /**
     * Creates a new brokerage account.
     *
     * @param name the account name
     * @param brokerName the broker name
     * @return the created account
     * @throws com.investments.tracker.application.exception.ResourceAlreadyExistsException if
     *     account name already exists
     */
    Account createAccount(AccountName name, BrokerName brokerName);
}
