import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ImportSessionResponse, MappingEntry, PriceEntry } from '../models';
import { API_BASE_URL } from './api.config';

@Injectable({
  providedIn: 'root',
})
export class ImportService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_BASE_URL);

  upload(broker: string, accountName: string, file: File): Observable<ImportSessionResponse> {
    const formData = new FormData();
    formData.append('broker', broker);
    formData.append('accountName', accountName);
    formData.append('file', file);
    return this.http.post<ImportSessionResponse>(`${this.apiUrl}/imports`, formData);
  }

  getSession(id: string): Observable<ImportSessionResponse> {
    return this.http.get<ImportSessionResponse>(`${this.apiUrl}/imports/${id}`);
  }

  confirm(id: string, mappings: MappingEntry[]): Observable<ImportSessionResponse> {
    return this.http.post<ImportSessionResponse>(`${this.apiUrl}/imports/${id}/confirm`, {
      mappings,
    });
  }

  providePrices(id: string, prices: PriceEntry[]): Observable<ImportSessionResponse> {
    return this.http.post<ImportSessionResponse>(`${this.apiUrl}/imports/${id}/prices`, {
      prices,
    });
  }
}
