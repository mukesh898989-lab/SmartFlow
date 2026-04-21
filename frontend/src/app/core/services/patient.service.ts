import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Patient, RegisterPatientRequest,
  Token, GenerateTokenRequest,
  QueuePositionResponse,
  Department, Doctor
} from '../models';

@Injectable({ providedIn: 'root' })
export class PatientService {

  private base    = `${environment.apiUrl}/patient`;
  private pubBase = `${environment.apiUrl}/public`;

  constructor(private http: HttpClient) {}

  selfRegister(request: RegisterPatientRequest): Observable<Patient> {
    return this.http.post<Patient>(`${this.base}/register`, request);
  }

  generateTokenOnline(request: GenerateTokenRequest): Observable<Token> {
    return this.http.post<Token>(`${this.base}/token`, request);
  }

  getTokenStatus(tokenNumber: string): Observable<Token> {
    return this.http.get<Token>(`${this.base}/token-status/${tokenNumber}`);
  }

  getQueuePosition(tokenNumber: string): Observable<QueuePositionResponse> {
    return this.http.get<QueuePositionResponse>(`${this.base}/queue-position/${tokenNumber}`);
  }

  // Public lookups (no auth)
  getPublicDepartments(): Observable<Department[]> {
    return this.http.get<Department[]>(`${this.pubBase}/departments`);
  }

  getPublicDoctors(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.pubBase}/doctors`);
  }

  getDoctorsByDepartment(departmentId: number): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.pubBase}/doctors/department/${departmentId}`);
  }
}
