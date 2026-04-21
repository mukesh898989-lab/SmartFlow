// ─── Enums ────────────────────────────────────────────────────────────────────

export type Role = 'ADMIN' | 'RECEPTIONIST' | 'DOCTOR';
export type Priority = 'NORMAL' | 'URGENT' | 'EMERGENCY';
export type TokenStatus = 'WAITING' | 'IN_PROGRESS' | 'COMPLETED';
export type RegistrationMode = 'SELF' | 'RECEPTIONIST';

// ─── Auth ─────────────────────────────────────────────────────────────────────

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  userId: number;
  name: string;
  email: string;
  role: Role;
}

// ─── Department ───────────────────────────────────────────────────────────────

export interface Department {
  id: number;
  name: string;
  description: string;
  active: boolean;
}

export interface CreateDepartmentRequest {
  name: string;
  description?: string;
}

// ─── Doctor ───────────────────────────────────────────────────────────────────

export interface Doctor {
  id: number;
  userId: number;
  name: string;
  email: string;
  specialization: string;
  available: boolean;
  department: Department;
}

export interface CreateDoctorRequest {
  name: string;
  email: string;
  password: string;
  departmentId: number;
  specialization?: string;
}

// ─── User ─────────────────────────────────────────────────────────────────────

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
  active: boolean;
  createdAt: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
  role: Role;
}

// ─── Patient ──────────────────────────────────────────────────────────────────

export interface Patient {
  id: number;
  name: string;
  phone: string;
  email?: string;
  age?: number;
  gender?: string;
  registeredBy: RegistrationMode;
  createdAt: string;
}

export interface RegisterPatientRequest {
  name: string;
  phone: string;
  email?: string;
  age?: number;
  gender?: string;
}

// ─── Token ────────────────────────────────────────────────────────────────────

export interface Token {
  id: number;
  tokenNumber: string;
  patientId: number;
  patientName: string;
  patientPhone: string;
  doctorId: number;
  doctorName: string;
  specialization: string;
  departmentId: number;
  departmentName: string;
  priority: Priority;
  status: TokenStatus;
  generatedBy: RegistrationMode;
  createdAt: string;
  updatedAt: string;
  queuePosition: number;
}

export interface GenerateTokenRequest {
  patientId: number;
  doctorId: number;
  priority?: Priority;
}

export interface UpdatePriorityRequest {
  priority: Priority;
}

export interface UpdateStatusRequest {
  status: TokenStatus;
}

export interface QueuePositionResponse {
  tokenNumber: string;
  status: TokenStatus;
  position: number;
  totalWaiting: number;
  doctorName: string;
  departmentName: string;
}
