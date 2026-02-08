import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
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
import { Formatters, TagSeverity } from '../../../core/utils';

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
  private readonly destroyRef = inject(DestroyRef);

  position = signal<PositionDetail | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const symbol = this.route.snapshot.paramMap.get('symbol');
    if (symbol) {
      this.loadPosition(symbol);
    } else {
      this.error.set('Position symbol is required');
      this.loading.set(false);
    }
  }

  loadPosition(id: string): void {
    this.loading.set(true);
    this.error.set(null);

    this.positionService.getPosition(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
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
