import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PatientService } from '../../../core/services/patient.service';
import { Department, Doctor, Patient, Token } from '../../../core/models';

type Step = 'register' | 'token' | 'done';

@Component({
  selector: 'app-get-token',
  templateUrl: './get-token.component.html'
})
export class GetTokenComponent implements OnInit {

  step: Step = 'register';

  registerForm: FormGroup;
  tokenForm: FormGroup;

  departments: Department[] = [];
  doctors: Doctor[] = [];
  filteredDoctors: Doctor[] = [];

  registeredPatient: Patient | null = null;
  generatedToken: Token | null = null;

  loading = false;
  error = '';

  constructor(private fb: FormBuilder, private patientService: PatientService) {
    this.registerForm = this.fb.group({
      name:   ['', [Validators.required, Validators.minLength(2)]],
      phone:  ['', [Validators.required, Validators.pattern(/^[0-9]{10,15}$/)]],
      email:  ['', Validators.email],
      age:    [null],
      gender: ['']
    });

    this.tokenForm = this.fb.group({
      departmentId: [null, Validators.required],
      doctorId:     [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.patientService.getPublicDepartments().subscribe(d => this.departments = d);
    this.patientService.getPublicDoctors().subscribe(d => this.doctors = d);

    this.tokenForm.get('departmentId')?.valueChanges.subscribe(deptId => {
      this.filteredDoctors = this.doctors.filter(d => d.department.id === +deptId && d.available);
      this.tokenForm.get('doctorId')?.reset();
    });
  }

  registerPatient(): void {
    if (this.registerForm.invalid) return;
    this.loading = true;
    this.error = '';

    const val = this.registerForm.value;
    const payload = {
      name:   val.name,
      phone:  val.phone,
      email:  val.email  || null,
      age:    val.age,
      gender: val.gender || null
    };

    this.patientService.selfRegister(payload).subscribe({
      next: patient => {
        this.registeredPatient = patient;
        this.step = 'token';
        this.loading = false;
      },
      error: err => {
        this.error = err.error?.message ?? 'Registration failed. Please try again.';
        this.loading = false;
      }
    });
  }

  generateToken(): void {
    if (this.tokenForm.invalid || !this.registeredPatient) return;
    this.loading = true;
    this.error = '';

    this.patientService.generateTokenOnline({
      patientId: this.registeredPatient.id,
      doctorId:  this.tokenForm.value.doctorId
      // priority is always NORMAL for online tokens — enforced server-side
    }).subscribe({
      next: token => {
        this.generatedToken = token;
        this.step = 'done';
        this.loading = false;
      },
      error: err => {
        this.error = err.error?.message ?? 'Token generation failed.';
        this.loading = false;
      }
    });
  }
}
