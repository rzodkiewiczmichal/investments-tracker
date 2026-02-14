package com.investments.tracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.domain.repository.AccountRepository;

@DisplayName("AccountQueryUseCaseService")
@ExtendWith(MockitoExtension.class)
class AccountQueryUseCaseServiceTest {

    @Mock private AccountRepository accountRepository;

    private AccountQueryUseCaseService accountQueryUseCaseService;

    @BeforeEach
    void setUp() {
        accountQueryUseCaseService = new AccountQueryUseCaseService(accountRepository);
    }

    @Nested
    @DisplayName("listAccounts")
    class ListAccounts {

        @Test
        @DisplayName("should return empty collection when no accounts")
        void shouldReturnEmptyCollectionWhenNoAccounts() {
            // Given
            when(accountRepository.findAll()).thenReturn(List.of());

            // When
            Collection<Account> accounts = accountQueryUseCaseService.listAccounts();

            // Then
            assertThat(accounts).isEmpty();
        }

        @Test
        @DisplayName("should return all accounts")
        void shouldReturnAllAccounts() {
            // Given
            Account account1 =
                    new Account(
                            new AccountId(1L), AccountName.of("Account 1"), BrokerName.of("XTB"));
            Account account2 =
                    new Account(
                            new AccountId(2L), AccountName.of("Account 2"), BrokerName.of("mBank"));

            when(accountRepository.findAll()).thenReturn(List.of(account1, account2));

            // When
            Collection<Account> accounts = accountQueryUseCaseService.listAccounts();

            // Then
            assertThat(accounts).hasSize(2);
            assertThat(accounts).containsExactlyInAnyOrder(account1, account2);
        }
    }
}
