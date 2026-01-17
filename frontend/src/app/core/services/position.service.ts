import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PositionsResponse, PositionDetail, AddPositionCommand } from '../models';
import { API_BASE_URL } from './api.config';

export type SortField = 'currentValue' | 'returnPercentage' | 'profitLoss' | 'quantity';
export type SortOrder = 'ASC' | 'DESC';

@Injectable({
  providedIn: 'root'
})
export class PositionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_BASE_URL);

  listPositions(sortBy: SortField = 'currentValue', order: SortOrder = 'DESC'): Observable<PositionsResponse> {
    const params = new HttpParams()
      .set('sortBy', sortBy)
      .set('order', order);

    return this.http.get<PositionsResponse>(`${this.apiUrl}/positions`, { params });
  }

  getPosition(id: string): Observable<PositionDetail> {
    return this.http.get<PositionDetail>(`${this.apiUrl}/positions/${id}`);
  }

  createPosition(command: AddPositionCommand): Observable<PositionDetail> {
    return this.http.post<PositionDetail>(`${this.apiUrl}/positions`, command);
  }
}
