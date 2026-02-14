import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Account, AccountsResponse, CreateAccountRequest } from '../models';
import { API_BASE_URL } from './api.config';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_BASE_URL);

  listAccounts(): Observable<AccountsResponse> {
    return this.http.get<AccountsResponse>(`${this.apiUrl}/accounts`);
  }

  createAccount(request: CreateAccountRequest): Observable<Account> {
    return this.http.post<Account>(`${this.apiUrl}/accounts`, request);
  }
}
