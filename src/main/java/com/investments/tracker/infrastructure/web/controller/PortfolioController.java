package com.investments.tracker.infrastructure.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investments.tracker.application.dto.mapper.PortfolioMapper;
import com.investments.tracker.application.dto.response.PortfolioSummaryResponse;
import com.investments.tracker.application.usecase.PortfolioQueryUseCase;
import com.investments.tracker.domain.model.Portfolio;

/** REST controller for portfolio operations. */
@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

    private final PortfolioQueryUseCase portfolioQueryUseCase;
    private final PortfolioMapper portfolioMapper;

    public PortfolioController(
            PortfolioQueryUseCase portfolioQueryUseCase, PortfolioMapper portfolioMapper) {
        this.portfolioQueryUseCase = portfolioQueryUseCase;
        this.portfolioMapper = portfolioMapper;
    }

    /**
     * Gets the portfolio summary.
     *
     * @return portfolio summary response
     */
    @GetMapping
    public PortfolioSummaryResponse getPortfolio() {
        Portfolio portfolio = portfolioQueryUseCase.getPortfolio();
        return portfolioMapper.toSummaryResponse(portfolio);
    }
}
