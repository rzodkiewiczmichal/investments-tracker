package com.investments.tracker.application.dto.response;

import java.util.List;

/**
 * Response containing list of position summaries.
 */
public record PositionListResponse(List<PositionSummaryDTO> positions, int totalCount) {
}
