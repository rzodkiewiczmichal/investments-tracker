import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { TableModule, TableRowSelectEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { SkeletonModule } from 'primeng/skeleton';
import { MessageModule } from 'primeng/message';
import { PortfolioService, PositionService } from '../../../core/services';
import { PortfolioSummary, PositionSummary, ApiError } from '../../../core/models';

@Component({
  selector: 'app-portfolio-view',
  standalone: true,
  imports: [CommonModule, CardModule, TableModule, ButtonModule, TagModule, SkeletonModule, MessageModule],
  templateUrl: './portfolio-view.component.html',
  styleUrl: './portfolio-view.component.scss'
})
export class PortfolioViewComponent implements OnInit {
  private readonly portfolioService = inject(PortfolioService);
  private readonly positionService = inject(PositionService);
  private readonly router = inject(Router);

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

  onRowSelect(event: TableRowSelectEvent<PositionSummary>): void {
    if (event.data && !Array.isArray(event.data)) {
      this.router.navigate(['/positions', event.data.id]);
    }
  }

  navigateToAdd(): void {
    this.router.navigate(['/positions/new']);
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

  getInstrumentTypeSeverity(type: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    switch (type) {
      case 'STOCK': return 'info';
      case 'ETF': return 'success';
      case 'BOND_ETF': return 'warn';
      case 'POLISH_GOV_BOND': return 'secondary';
      default: return 'secondary';
    }
  }
}
