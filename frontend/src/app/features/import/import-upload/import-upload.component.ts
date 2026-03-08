import { Component, DestroyRef, EventEmitter, Output, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { FileUploadModule } from 'primeng/fileupload';
import { MessageModule } from 'primeng/message';
import { ImportService } from '../../../core/services';
import { ApiError, ImportSessionResponse } from '../../../core/models';

@Component({
  selector: 'app-import-upload',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    ButtonModule,
    InputTextModule,
    FileUploadModule,
    MessageModule,
  ],
  templateUrl: './import-upload.component.html',
  styleUrl: './import-upload.component.scss',
})
export class ImportUploadComponent {
  @Output() uploaded = new EventEmitter<ImportSessionResponse>();

  private readonly importService = inject(ImportService);
  private readonly destroyRef = inject(DestroyRef);

  broker = signal('');
  accountName = signal('');
  selectedFile = signal<File | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);

  onFileSelect(event: { files: File[] }): void {
    if (event.files.length > 0) {
      this.selectedFile.set(event.files[0]);
    }
  }

  onFileClear(): void {
    this.selectedFile.set(null);
  }

  canUpload(): boolean {
    return !!this.broker() && !!this.accountName() && !!this.selectedFile();
  }

  upload(): void {
    const broker = this.broker();
    const account = this.accountName();
    const file = this.selectedFile();
    if (!broker || !account || !file) return;

    this.loading.set(true);
    this.error.set(null);

    this.importService
      .upload(broker, account, file)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (session) => {
          this.loading.set(false);
          this.uploaded.emit(session);
        },
        error: (err: ApiError) => {
          this.loading.set(false);
          this.error.set(err.message || 'Failed to upload file');
        },
      });
  }
}
