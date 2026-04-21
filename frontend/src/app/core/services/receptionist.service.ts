import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Patient, RegisterPatientRequest,
  Token, GenerateTokenRequest, UpdatePriorityRequest
} from '../models';

@Injectable({ providedIn: 'root' })
export class ReceptionistService {

  private base = `${environment.apiUrl}/receptionist`;

  constructor(private http: HttpClient) {}

  // Patients
  registerPatient(request: RegisterPatientRequest): Observable<Patient> {
    return this.http.post<Patient>(`${this.base}/patients`, request);
  }
  getPatients(): Observable<Patient[]> {
    return this.http.get<Patient[]>(`${this.base}/patients`);
  }

  // Tokens
  generateToken(request: GenerateTokenRequest): Observable<Token> {
    return this.http.post<Token>(`${this.base}/tokens`, request);
  }
  updatePriority(tokenId: number, request: UpdatePriorityRequest): Observable<Token> {
    return this.http.put<Token>(`${this.base}/tokens/${tokenId}/priority`, request);
  }
  removeToken(tokenId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/tokens/${tokenId}`);
  }

  // Queue
  getActiveQueue(): Observable<Token[]> {
    return this.http.get<Token[]>(`${this.base}/queue`);
  }
}
