import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CardModule } from 'primeng/card';
import { TableModule, TableRowSelectEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { SkeletonModule } from 'primeng/skeleton';
import { MessageModule } from 'primeng/message';
import { PortfolioService, PositionService } from '../../../core/services';
import { PortfolioSummary, PositionSummary, ApiError } from '../../../core/models';
import { Formatters, TagSeverity } from '../../../core/utils';

@Component({
  selector: 'app-portfolio-view',
  standalone: true,
  imports: [
    CommonModule,
    CardModule,
    TableModule,
    ButtonModule,
    TagModule,
    SkeletonModule,
    MessageModule,
  ],
  templateUrl: './portfolio-view.component.html',
  styleUrl: './portfolio-view.component.scss',
})
export class PortfolioViewComponent implements OnInit {
  private readonly portfolioService = inject(PortfolioService);
  private readonly positionService = inject(PositionService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

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

    forkJoin({
      portfolio: this.portfolioService.getPortfolioSummary(),
      positions: this.positionService.listPositions(),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ portfolio, positions }) => {
          this.portfolio.set(portfolio);
          this.positions.set(positions.positions);
          this.loading.set(false);
        },
        error: (err: ApiError) => {
          this.error.set(err.message);
          this.loading.set(false);
        },
      });
  }

  onRowSelect(event: TableRowSelectEvent<PositionSummary>): void {
    if (event.data && !Array.isArray(event.data)) {
      this.router.navigate(['/positions', event.data.instrumentSymbol]);
    }
  }

  navigateToImport(): void {
    this.router.navigate(['/import']);
  }

  navigateToAdd(): void {
    this.router.navigate(['/positions/new']);
  }

  formatMoney(amount: number | null | undefined): string {
    return Formatters.formatMoney(amount);
  }

  formatPercentage(value: number | null | undefined): string {
    return Formatters.formatPercentage(value);
  }

  formatQuantity(quantity: number): string {
    return Formatters.formatQuantity(quantity);
  }

  getProfitLossClass(value: number | null | undefined): string {
    return Formatters.getProfitLossClass(value);
  }

  getInstrumentTypeSeverity(type: string): TagSeverity {
    return Formatters.getInstrumentTypeSeverity(type);
  }
}
