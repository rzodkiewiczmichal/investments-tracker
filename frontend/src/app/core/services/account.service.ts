import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountsResponse } from '../models';
import { API_BASE_URL } from './api.config';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_BASE_URL);

  listAccounts(): Observable<AccountsResponse> {
    return this.http.get<AccountsResponse>(`${this.apiUrl}/accounts`);
  }
}
