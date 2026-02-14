import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { SkeletonModule } from 'primeng/skeleton';
import { FloatLabelModule } from 'primeng/floatlabel';
import { DialogModule } from 'primeng/dialog';
import { TooltipModule } from 'primeng/tooltip';
import { PositionService, AccountService } from '../../../core/services';
import {
  Account,
  CreateAccountRequest,
  InstrumentType,
  CurrencyCode,
  ApiError,
  ValidationError,
} from '../../../core/models';

@Component({
  selector: 'app-position-entry',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardModule,
    ButtonModule,
    InputTextModule,
    SelectModule,
    InputNumberModule,
    MessageModule,
    SkeletonModule,
    FloatLabelModule,
    DialogModule,
    TooltipModule,
  ],
  templateUrl: './position-entry.component.html',
  styleUrl: './position-entry.component.scss',
})
export class PositionEntryComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly positionService = inject(PositionService);
  private readonly accountService = inject(AccountService);
  private readonly destroyRef = inject(DestroyRef);

  form!: FormGroup;
  accountForm!: FormGroup;
  accounts = signal<Account[]>([]);
  loading = signal(false);
  loadingAccounts = signal(true);
  error = signal<string | null>(null);
  fieldErrors = signal<Map<string, string>>(new Map());

  showAccountDialog = signal(false);
  accountDialogLoading = signal(false);
  accountDialogError = signal<string | null>(null);

  instrumentTypes: { value: InstrumentType; label: string }[] = [
    { value: 'STOCK', label: 'Stock' },
    { value: 'ETF', label: 'ETF' },
  ];

  currencies: { value: CurrencyCode; label: string }[] = [
    { value: 'PLN', label: 'PLN' },
    { value: 'EUR', label: 'EUR' },
    { value: 'USD', label: 'USD' },
    { value: 'GBP', label: 'GBP' },
  ];

  ngOnInit(): void {
    this.initForm();
    this.initAccountForm();
    this.loadAccounts();
  }

  private initForm(): void {
    this.form = this.fb.group({
      instrumentName: ['', [Validators.required, Validators.maxLength(255)]],
      instrumentSymbol: ['', [Validators.required, Validators.maxLength(50)]],
      instrumentType: ['STOCK', [Validators.required]],
      currency: ['PLN', [Validators.required]],
      accountId: [null, [Validators.required]],
      quantity: [null, [Validators.required, Validators.min(0.00000001)]],
      averageCost: [null, [Validators.required, Validators.min(0.0001)]],
    });
  }

  private initAccountForm(): void {
    this.accountForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      broker: ['', [Validators.required, Validators.maxLength(255)]],
    });
  }

  private loadAccounts(): void {
    this.accountService
      .listAccounts()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.accounts.set(response.accounts);
          if (response.accounts.length === 1) {
            this.form.patchValue({ accountId: response.accounts[0].id });
          }
          this.loadingAccounts.set(false);
        },
        error: (err: ApiError) => {
          this.error.set(`Failed to load accounts: ${err.message}`);
          this.loadingAccounts.set(false);
        },
      });
  }

  goBack(): void {
    this.router.navigate(['/portfolio']);
  }

  openAccountDialog(): void {
    this.accountForm.reset();
    this.accountDialogError.set(null);
    this.showAccountDialog.set(true);
  }

  submitAccountDialog(): void {
    if (this.accountForm.invalid) {
      this.accountForm.markAllAsTouched();
      return;
    }

    this.accountDialogLoading.set(true);
    this.accountDialogError.set(null);

    const request: CreateAccountRequest = this.accountForm.value;

    this.accountService
      .createAccount(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (newAccount) => {
          this.accounts.update((current) => [...current, newAccount]);
          this.form.patchValue({ accountId: newAccount.id });
          this.accountDialogLoading.set(false);
          this.showAccountDialog.set(false);
        },
        error: (err: ApiError) => {
          this.accountDialogLoading.set(false);
          this.accountDialogError.set(err.message);
        },
      });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    this.fieldErrors.set(new Map());

    const command = this.form.value;

    this.positionService
      .createPosition(command)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (position) => {
          this.router.navigate(['/positions', position.instrumentSymbol]);
        },
        error: (err: ApiError) => {
          this.loading.set(false);

          if (err.details && err.details.length > 0) {
            const errors = new Map<string, string>();
            err.details.forEach((detail: ValidationError) => {
              errors.set(detail.field, detail.message);
            });
            this.fieldErrors.set(errors);
            this.error.set('Please fix the validation errors below.');
          } else {
            this.error.set(err.message);
          }
        },
      });
  }

  getFieldError(fieldName: string): string | null {
    const serverError = this.fieldErrors().get(fieldName);
    if (serverError) {
      return serverError;
    }

    const control = this.form.get(fieldName);
    if (control?.touched && control?.errors) {
      if (control.errors['required']) {
        return 'This field is required';
      }
      if (control.errors['min']) {
        return `Value must be greater than ${control.errors['min'].min}`;
      }
      if (control.errors['maxlength']) {
        return `Maximum length is ${control.errors['maxlength'].requiredLength} characters`;
      }
    }

    return null;
  }

  hasFieldError(fieldName: string): boolean {
    return this.getFieldError(fieldName) !== null;
  }
}
