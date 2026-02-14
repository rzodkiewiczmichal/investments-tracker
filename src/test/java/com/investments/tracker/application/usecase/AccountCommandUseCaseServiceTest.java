package com.investments.tracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investments.tracker.application.exception.ResourceAlreadyExistsException;
import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.domain.repository.AccountRepository;

@DisplayName("AccountCommandUseCaseService")
@ExtendWith(MockitoExtension.class)
class AccountCommandUseCaseServiceTest {

    @Mock private AccountRepository accountRepository;

    private AccountCommandUseCaseService service;

    @BeforeEach
    void setUp() {
        service = new AccountCommandUseCaseService(accountRepository);
    }

    @Nested
    @DisplayName("createAccount")
    class CreateAccount {

        @Test
        @DisplayName("should create account when name is unique")
        void shouldCreateAccountWhenNameIsUnique() {
            // Given
            AccountName name = new AccountName("XTB");
            BrokerName broker = new BrokerName("XTB");
            Account expected = new Account(new AccountId(1L), name, broker);

            when(accountRepository.findByName(name)).thenReturn(Optional.empty());
            when(accountRepository.create(name, broker)).thenReturn(expected);

            // When
            Account result = service.createAccount(name, broker);

            // Then
            assertThat(result.id().value()).isEqualTo(1L);
            assertThat(result.name().value()).isEqualTo("XTB");
            assertThat(result.brokerName().value()).isEqualTo("XTB");
            verify(accountRepository).create(name, broker);
        }

        @Test
        @DisplayName("should throw ResourceAlreadyExistsException when name already exists")
        void shouldThrowWhenNameAlreadyExists() {
            // Given
            AccountName name = new AccountName("XTB");
            BrokerName broker = new BrokerName("XTB");
            Account existing = new Account(new AccountId(1L), name, broker);

            when(accountRepository.findByName(name)).thenReturn(Optional.of(existing));

            // When/Then
            assertThatThrownBy(() -> service.createAccount(name, broker))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Account")
                    .hasMessageContaining("XTB");

            verify(accountRepository, never()).create(any(), any());
        }

        @Test
        @DisplayName("should throw NullPointerException when name is null")
        void shouldThrowWhenNameIsNull() {
            assertThatThrownBy(() -> service.createAccount(null, new BrokerName("XTB")))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException when brokerName is null")
        void shouldThrowWhenBrokerNameIsNull() {
            assertThatThrownBy(() -> service.createAccount(new AccountName("XTB"), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
