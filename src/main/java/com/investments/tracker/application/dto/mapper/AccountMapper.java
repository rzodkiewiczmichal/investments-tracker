package com.investments.tracker.application.dto.mapper;

import com.investments.tracker.application.dto.response.AccountDTO;
import com.investments.tracker.domain.model.Account;
import org.springframework.stereotype.Component;

/**
 * Mapper for Account domain objects to DTOs.
 */
@Component
public class AccountMapper {

    /**
     * Maps an Account to AccountDTO.
     *
     * @param account the account domain object
     * @return the account DTO
     */
    public AccountDTO toDTO(Account account) {
        return new AccountDTO(
                account.id().value(),
                account.name().value(),
                account.brokerName().value());
    }
}
