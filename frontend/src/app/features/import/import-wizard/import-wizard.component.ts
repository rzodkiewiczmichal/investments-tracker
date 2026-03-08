import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { StepperModule } from 'primeng/stepper';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ImportUploadComponent } from '../import-upload/import-upload.component';
import { ImportMappingComponent } from '../import-mapping/import-mapping.component';
import { ImportPricesComponent } from '../import-prices/import-prices.component';
import { ImportConfirmationComponent } from '../import-confirmation/import-confirmation.component';
import { ImportService } from '../../../core/services';
import { ApiError, ImportSessionResponse } from '../../../core/models';

@Component({
  selector: 'app-import-wizard',
  standalone: true,
  imports: [
    CommonModule,
    StepperModule,
    ButtonModule,
    MessageModule,
    ImportUploadComponent,
    ImportMappingComponent,
    ImportPricesComponent,
    ImportConfirmationComponent,
  ],
  templateUrl: './import-wizard.component.html',
  styleUrl: './import-wizard.component.scss',
})
export class ImportWizardComponent {
  private readonly importService = inject(ImportService);
  private readonly destroyRef = inject(DestroyRef);

  activeStep = signal(0);
  session = signal<ImportSessionResponse | null>(null);
  autoConfirming = signal(false);
  error = signal<string | null>(null);

  onUploaded(session: ImportSessionResponse): void {
    this.session.set(session);

    if (session.status === 'READY_TO_CONFIRM') {
      this.autoConfirm(session);
    } else {
      this.activeStep.set(1);
    }
  }

  onMappingConfirmed(session: ImportSessionResponse): void {
    this.session.set(session);

    if (session.status === 'PENDING_PRICES') {
      this.activeStep.set(2);
    } else {
      this.activeStep.set(3);
    }
  }

  onPricesProvided(session: ImportSessionResponse): void {
    this.session.set(session);
    this.activeStep.set(3);
  }

  onImportAnother(): void {
    this.session.set(null);
    this.activeStep.set(0);
    this.error.set(null);
    this.autoConfirming.set(false);
  }

  private autoConfirm(session: ImportSessionResponse): void {
    this.autoConfirming.set(true);
    this.error.set(null);

    this.importService
      .confirm(session.importSessionId, [])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (confirmed) => {
          this.autoConfirming.set(false);
          this.session.set(confirmed);

          if (confirmed.status === 'PENDING_PRICES') {
            this.activeStep.set(2);
          } else {
            this.activeStep.set(3);
          }
        },
        error: (err: ApiError) => {
          this.autoConfirming.set(false);
          this.error.set(err.message || 'Failed to confirm import');
        },
      });
  }
}
