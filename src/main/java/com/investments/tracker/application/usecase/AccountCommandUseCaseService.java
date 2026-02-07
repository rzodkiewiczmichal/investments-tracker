package com.investments.tracker.application.usecase;

import com.investments.tracker.application.exception.ResourceAlreadyExistsException;
import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.domain.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Implementation of account command use case.
 */
@Service
@Transactional
public class AccountCommandUseCaseService implements AccountCommandUseCase {

    private final AccountRepository accountRepository;

    public AccountCommandUseCaseService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(AccountName name, BrokerName brokerName) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(brokerName, "brokerName cannot be null");

        if (accountRepository.findByName(name).isPresent()) {
            throw new ResourceAlreadyExistsException("Account", "name", name.value());
        }

        return accountRepository.create(name, brokerName);
    }
}
