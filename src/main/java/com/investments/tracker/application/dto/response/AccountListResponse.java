package com.investments.tracker.application.dto.response;

import java.util.List;

/** Response containing list of accounts. */
public record AccountListResponse(List<AccountDTO> accounts, int totalCount) {}
