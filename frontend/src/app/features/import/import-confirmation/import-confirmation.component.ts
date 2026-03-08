import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ImportSessionResponse } from '../../../core/models';

@Component({
  selector: 'app-import-confirmation',
  standalone: true,
  imports: [CommonModule, RouterModule, CardModule, ButtonModule, MessageModule],
  templateUrl: './import-confirmation.component.html',
  styleUrl: './import-confirmation.component.scss',
})
export class ImportConfirmationComponent {
  @Input({ required: true }) session!: ImportSessionResponse;
  @Output() importAnother = new EventEmitter<void>();
}
