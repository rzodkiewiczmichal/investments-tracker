import {
  Component,
  DestroyRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { MessageModule } from 'primeng/message';
import { DividerModule } from 'primeng/divider';
import { AutoCompleteModule, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { ImportService, InstrumentService } from '../../../core/services';
import {
  ApiError,
  ImportSessionResponse,
  InstrumentDTO,
  MappingEntry,
  UnmatchedInstrument,
} from '../../../core/models';

interface MappingState {
  brokerName: string;
  selectedInstrument: InstrumentDTO | null;
  suggestions: { symbol: string; name: string }[];
  searchResults: InstrumentDTO[];
}

@Component({
  selector: 'app-import-mapping',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    ButtonModule,
    TagModule,
    MessageModule,
    DividerModule,
    AutoCompleteModule,
  ],
  templateUrl: './import-mapping.component.html',
  styleUrl: './import-mapping.component.scss',
})
export class ImportMappingComponent implements OnInit {
  @Input({ required: true }) session!: ImportSessionResponse;
  @Output() confirmed = new EventEmitter<ImportSessionResponse>();

  private readonly importService = inject(ImportService);
  private readonly instrumentService = inject(InstrumentService);
  private readonly destroyRef = inject(DestroyRef);

  mappings = signal<MappingState[]>([]);
  confirming = signal(false);
  error = signal<string | null>(null);

  allMapped = computed(() => this.mappings().every((m) => m.selectedInstrument !== null));

  ngOnInit(): void {
    const unmatchedDetails = this.session.summary.unmatchedDetails;
    this.mappings.set(
      unmatchedDetails.map((u: UnmatchedInstrument) => ({
        brokerName: u.brokerName,
        selectedInstrument: null,
        suggestions: u.suggestions,
        searchResults: [],
      })),
    );
  }

  searchInstruments(event: AutoCompleteCompleteEvent, index: number): void {
    const query = event.query;
    if (query.length < 1) return;

    this.instrumentService
      .searchInstruments(query)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.mappings.update((current) => {
            const updated = [...current];
            updated[index] = { ...updated[index], searchResults: response.instruments };
            return updated;
          });
        },
      });
  }

  onInstrumentSelect(event: { value: InstrumentDTO }, index: number): void {
    this.mappings.update((current) => {
      const updated = [...current];
      updated[index] = { ...updated[index], selectedInstrument: event.value };
      return updated;
    });
  }

  onInstrumentClear(index: number): void {
    this.mappings.update((current) => {
      const updated = [...current];
      updated[index] = { ...updated[index], selectedInstrument: null };
      return updated;
    });
  }

  useSuggestion(index: number, symbol: string, name: string): void {
    const instrument: InstrumentDTO = { symbol, name, type: '', currency: '' };
    this.mappings.update((current) => {
      const updated = [...current];
      updated[index] = { ...updated[index], selectedInstrument: instrument };
      return updated;
    });
  }

  confirm(): void {
    const entries: MappingEntry[] = this.mappings()
      .filter((m) => m.selectedInstrument !== null)
      .map((m) => ({
        brokerName: m.brokerName,
        catalogSymbol: m.selectedInstrument!.symbol,
      }));

    this.confirming.set(true);
    this.error.set(null);

    this.importService
      .confirm(this.session.importSessionId, entries)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (session) => {
          this.confirming.set(false);
          this.confirmed.emit(session);
        },
        error: (err: ApiError) => {
          this.confirming.set(false);
          this.error.set(err.message || 'Failed to confirm import');
        },
      });
  }
}
