package com.investments.tracker.application.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.investments.tracker.application.dto.response.AccountDTO;
import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerName;

@DisplayName("AccountMapper")
class AccountMapperTest {

    private final AccountMapper mapper = new AccountMapper();

    @Nested
    @DisplayName("toDTO")
    class ToDTO {

        @Test
        @DisplayName("should map account to DTO")
        void shouldMapAccountToDTO() {
            // Given
            Account account =
                    new Account(
                            new AccountId(1L), AccountName.of("My Account"), BrokerName.of("XTB"));

            // When
            AccountDTO dto = mapper.toDTO(account);

            // Then
            assertThat(dto.id()).isEqualTo(1L);
            assertThat(dto.name()).isEqualTo("My Account");
            assertThat(dto.broker()).isEqualTo("XTB");
        }
    }
}
