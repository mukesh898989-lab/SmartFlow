import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  User, CreateUserRequest,
  Department, CreateDepartmentRequest,
  Doctor, CreateDoctorRequest,
  Token
} from '../models';

@Injectable({ providedIn: 'root' })
export class AdminService {

  private base = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  // Users
  createUser(request: CreateUserRequest): Observable<User> {
    return this.http.post<User>(`${this.base}/users`, request);
  }
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/users`);
  }
  activateUser(id: number): Observable<User> {
    return this.http.put<User>(`${this.base}/users/${id}/activate`, {});
  }
  deactivateUser(id: number): Observable<User> {
    return this.http.put<User>(`${this.base}/users/${id}/deactivate`, {});
  }

  // Departments
  createDepartment(request: CreateDepartmentRequest): Observable<Department> {
    return this.http.post<Department>(`${this.base}/departments`, request);
  }
  getDepartments(): Observable<Department[]> {
    return this.http.get<Department[]>(`${this.base}/departments`);
  }
  updateDepartment(id: number, request: CreateDepartmentRequest): Observable<Department> {
    return this.http.put<Department>(`${this.base}/departments/${id}`, request);
  }

  // Doctors
  createDoctor(request: CreateDoctorRequest): Observable<Doctor> {
    return this.http.post<Doctor>(`${this.base}/doctors`, request);
  }
  getDoctors(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.base}/doctors`);
  }
  updateDoctor(id: number, request: CreateDoctorRequest): Observable<Doctor> {
    return this.http.put<Doctor>(`${this.base}/doctors/${id}`, request);
  }

  // Queue overview
  getAllActiveTokens(): Observable<Token[]> {
    return this.http.get<Token[]>(`${this.base}/queue`);
  }
}
