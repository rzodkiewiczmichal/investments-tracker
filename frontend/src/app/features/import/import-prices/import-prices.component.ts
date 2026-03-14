import {
  Component,
  DestroyRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  inject,
  signal,
  computed,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { ImportService } from '../../../core/services';
import {
  ApiError,
  ImportSessionResponse,
  InstrumentNeedingPrice,
  PriceEntry,
} from '../../../core/models';

interface PriceState {
  symbol: string;
  name: string;
  currency: string;
  price: number | null;
}

@Component({
  selector: 'app-import-prices',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, ButtonModule, InputNumberModule, MessageModule],
  templateUrl: './import-prices.component.html',
  styleUrl: './import-prices.component.scss',
})
export class ImportPricesComponent implements OnInit {
  @Input({ required: true }) session!: ImportSessionResponse;
  @Output() pricesProvided = new EventEmitter<ImportSessionResponse>();

  private readonly importService = inject(ImportService);
  private readonly destroyRef = inject(DestroyRef);

  priceStates = signal<PriceState[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);

  allPricesProvided = computed(() =>
    this.priceStates().every((s) => s.price !== null && s.price > 0),
  );

  ngOnInit(): void {
    const instruments: InstrumentNeedingPrice[] =
      this.session.summary.instrumentsNeedingPrices || [];
    this.priceStates.set(
      instruments.map((i) => ({
        symbol: i.symbol,
        name: i.name,
        currency: i.currency,
        price: null,
      })),
    );
  }

  onPriceChange(index: number, value: number | null): void {
    this.priceStates.update((states) => {
      const updated = [...states];
      updated[index] = { ...updated[index], price: value };
      return updated;
    });
  }

  submit(): void {
    const prices: PriceEntry[] = this.priceStates()
      .filter((s) => s.price !== null && s.price > 0)
      .map((s) => ({
        symbol: s.symbol,
        price: s.price!.toString(),
        currency: s.currency,
      }));

    this.loading.set(true);
    this.error.set(null);

    this.importService
      .providePrices(this.session.importSessionId, prices)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (session) => {
          this.loading.set(false);
          this.pricesProvided.emit(session);
        },
        error: (err: ApiError) => {
          this.loading.set(false);
          this.error.set(err.message || 'Failed to submit prices');
        },
      });
  }
}
