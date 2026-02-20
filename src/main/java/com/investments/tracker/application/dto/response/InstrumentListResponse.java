package com.investments.tracker.application.dto.response;

import java.util.List;

/** Response containing list of instruments. */
public record InstrumentListResponse(List<InstrumentDTO> instruments, int totalCount) {}
