package com.investments.tracker.infrastructure.web.controller;

import com.investments.tracker.application.dto.mapper.AccountMapper;
import com.investments.tracker.application.dto.request.CreateAccountRequest;
import com.investments.tracker.application.dto.response.AccountDTO;
import com.investments.tracker.application.dto.response.AccountListResponse;
import com.investments.tracker.application.usecase.AccountCommandUseCase;
import com.investments.tracker.application.usecase.AccountQueryUseCase;
import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerName;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

/**
 * REST controller for account operations.
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountQueryUseCase accountQueryUseCase;
    private final AccountCommandUseCase accountCommandUseCase;
    private final AccountMapper accountMapper;

    public AccountController(
            AccountQueryUseCase accountQueryUseCase,
            AccountCommandUseCase accountCommandUseCase,
            AccountMapper accountMapper) {
        this.accountQueryUseCase = accountQueryUseCase;
        this.accountCommandUseCase = accountCommandUseCase;
        this.accountMapper = accountMapper;
    }

    /**
     * Lists all accounts.
     *
     * @return account list response
     */
    @GetMapping
    public AccountListResponse listAccounts() {
        Collection<Account> accounts = accountQueryUseCase.listAccounts();
        List<AccountDTO> accountDTOs = accounts.stream()
                .map(accountMapper::toDTO)
                .toList();
        return new AccountListResponse(accountDTOs, accountDTOs.size());
    }

    /**
     * Gets a specific account by ID.
     *
     * @param id the account ID
     * @return the account DTO
     */
    @GetMapping("/{id}")
    public AccountDTO getAccount(@PathVariable Long id) {
        Account account = accountQueryUseCase.getAccount(new AccountId(id));
        return accountMapper.toDTO(account);
    }

    /**
     * Creates a new brokerage account.
     *
     * @param request the create account request
     * @return the created account DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountCommandUseCase.createAccount(
                new AccountName(request.name()),
                new BrokerName(request.broker()));
        return accountMapper.toDTO(account);
    }
}
