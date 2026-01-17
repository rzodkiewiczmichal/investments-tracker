import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PortfolioService, PositionService } from '../../../core/services';
import { PortfolioSummary, PositionSummary, ApiError } from '../../../core/models';

@Component({
  selector: 'app-portfolio-view',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './portfolio-view.component.html',
  styleUrl: './portfolio-view.component.scss'
})
export class PortfolioViewComponent implements OnInit {
  private readonly portfolioService = inject(PortfolioService);
  private readonly positionService = inject(PositionService);

  portfolio = signal<PortfolioSummary | null>(null);
  positions = signal<PositionSummary[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.error.set(null);

    this.portfolioService.getPortfolioSummary().subscribe({
      next: (portfolio) => {
        this.portfolio.set(portfolio);
      },
      error: (err: ApiError) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });

    this.positionService.listPositions().subscribe({
      next: (response) => {
        this.positions.set(response.positions);
        this.loading.set(false);
      },
      error: (err: ApiError) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }

  formatMoney(amount: number): string {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  }

  formatPercentage(value: number): string {
    const sign = value >= 0 ? '+' : '';
    return `${sign}${value.toFixed(2)}%`;
  }

  formatQuantity(quantity: number): string {
    return new Intl.NumberFormat('pl-PL', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 8
    }).format(quantity);
  }

  getProfitLossClass(value: number): string {
    if (value > 0) return 'positive';
    if (value < 0) return 'negative';
    return 'neutral';
  }
}
