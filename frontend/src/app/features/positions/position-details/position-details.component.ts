import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { SkeletonModule } from 'primeng/skeleton';
import { MessageModule } from 'primeng/message';
import { DividerModule } from 'primeng/divider';
import { PositionService } from '../../../core/services';
import { PositionDetail, ApiError } from '../../../core/models';

@Component({
  selector: 'app-position-details',
  standalone: true,
  imports: [CommonModule, CardModule, ButtonModule, TagModule, SkeletonModule, MessageModule, DividerModule],
  templateUrl: './position-details.component.html',
  styleUrl: './position-details.component.scss'
})
export class PositionDetailsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly positionService = inject(PositionService);

  position = signal<PositionDetail | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadPosition(id);
    } else {
      this.error.set('Position ID is required');
      this.loading.set(false);
    }
  }

  loadPosition(id: string): void {
    this.loading.set(true);
    this.error.set(null);

    this.positionService.getPosition(id).subscribe({
      next: (position) => {
        this.position.set(position);
        this.loading.set(false);
      },
      error: (err: ApiError) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/portfolio']);
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

  formatDate(dateString: string): string {
    return new Intl.DateTimeFormat('pl-PL', {
      dateStyle: 'medium',
      timeStyle: 'short'
    }).format(new Date(dateString));
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
