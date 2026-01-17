import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PositionService, AccountService } from '../../../core/services';
import { Account, InstrumentType, ApiError, ValidationError } from '../../../core/models';

@Component({
  selector: 'app-position-entry',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './position-entry.component.html',
  styleUrl: './position-entry.component.scss'
})
export class PositionEntryComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly positionService = inject(PositionService);
  private readonly accountService = inject(AccountService);

  form!: FormGroup;
  accounts = signal<Account[]>([]);
  loading = signal(false);
  loadingAccounts = signal(true);
  error = signal<string | null>(null);
  fieldErrors = signal<Map<string, string>>(new Map());

  instrumentTypes: { value: InstrumentType; label: string }[] = [
    { value: 'STOCK', label: 'Stock' },
    { value: 'ETF', label: 'ETF' }
  ];

  ngOnInit(): void {
    this.initForm();
    this.loadAccounts();
  }

  private initForm(): void {
    this.form = this.fb.group({
      instrumentName: ['', [Validators.required, Validators.maxLength(255)]],
      instrumentSymbol: ['', [Validators.required, Validators.maxLength(50)]],
      instrumentType: ['STOCK', [Validators.required]],
      accountId: ['', [Validators.required]],
      quantity: [null, [Validators.required, Validators.min(0.00000001)]],
      averageCost: [null, [Validators.required, Validators.min(0.0001)]]
    });
  }

  private loadAccounts(): void {
    this.accountService.listAccounts().subscribe({
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
      }
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

    this.positionService.createPosition(command).subscribe({
      next: (position) => {
        this.router.navigate(['/positions', position.id]);
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
      }
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
