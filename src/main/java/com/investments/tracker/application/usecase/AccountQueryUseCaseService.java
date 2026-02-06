package com.investments.tracker.application.usecase;

import com.investments.tracker.application.exception.ResourceNotFoundException;
import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * Implementation of account query use case.
 */
@Service
@Transactional(readOnly = true)
public class AccountQueryUseCaseService implements AccountQueryUseCase {

    private final AccountRepository accountRepository;

    public AccountQueryUseCaseService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Collection<Account> listAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Account getAccount(AccountId id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", String.valueOf(id.value())));
    }
}
