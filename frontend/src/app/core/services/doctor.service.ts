import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Token, UpdateStatusRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class DoctorService {

  private base = `${environment.apiUrl}/doctor`;

  constructor(private http: HttpClient) {}

  getMyQueue(): Observable<Token[]> {
    return this.http.get<Token[]>(`${this.base}/queue`);
  }

  callNextPatient(): Observable<Token> {
    return this.http.post<Token>(`${this.base}/queue/next`, {});
  }

  updateTokenStatus(tokenId: number, request: UpdateStatusRequest): Observable<Token> {
    return this.http.put<Token>(`${this.base}/tokens/${tokenId}/status`, request);
  }
}
