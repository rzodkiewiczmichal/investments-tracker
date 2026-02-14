import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PositionsResponse, PositionDetail, AddPositionCommand } from '../models';
import { API_BASE_URL } from './api.config';

@Injectable({
  providedIn: 'root',
})
export class PositionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_BASE_URL);

  listPositions(): Observable<PositionsResponse> {
    return this.http.get<PositionsResponse>(`${this.apiUrl}/positions`);
  }

  getPosition(symbol: string): Observable<PositionDetail> {
    return this.http.get<PositionDetail>(`${this.apiUrl}/positions/${symbol}`);
  }

  createPosition(command: AddPositionCommand): Observable<PositionDetail> {
    return this.http.post<PositionDetail>(`${this.apiUrl}/positions`, command);
  }
}
