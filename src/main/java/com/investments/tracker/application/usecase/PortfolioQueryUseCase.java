package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.Portfolio;

/** Use case for querying portfolio information. */
public interface PortfolioQueryUseCase {

    /**
     * Gets the aggregated portfolio.
     *
     * @return the portfolio with all positions and metrics
     */
    Portfolio getPortfolio();
}
