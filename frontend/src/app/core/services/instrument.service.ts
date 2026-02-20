import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InstrumentListResponse } from '../models';
import { API_BASE_URL } from './api.config';

@Injectable({
  providedIn: 'root',
})
export class InstrumentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_BASE_URL);

  searchInstruments(query: string): Observable<InstrumentListResponse> {
    return this.http.get<InstrumentListResponse>(`${this.apiUrl}/instruments`, {
      params: { q: query },
    });
  }
}
