import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReceptionistService } from '../../../core/services/receptionist.service';
import { PatientService } from '../../../core/services/patient.service';
import { Department, Doctor, Patient, Token } from '../../../core/models';

@Component({
  selector: 'app-generate-token',
  templateUrl: './generate-token.component.html'
})
export class GenerateTokenComponent implements OnInit {

  patients: Patient[] = [];
  departments: Department[] = [];
  doctors: Doctor[] = [];
  filteredDoctors: Doctor[] = [];

  form: FormGroup;
  loading = false;
  success = '';
  error = '';
  generatedToken: Token | null = null;

  constructor(
    private fb: FormBuilder,
    private receptionistService: ReceptionistService,
    private patientService: PatientService
  ) {
    this.form = this.fb.group({
      patientId:    [null, Validators.required],
      departmentId: [null, Validators.required],
      doctorId:     [null, Validators.required],
      priority:     ['NORMAL', Validators.required]
    });
  }

  ngOnInit(): void {
    this.receptionistService.getPatients().subscribe(p => this.patients = p);
    this.patientService.getPublicDepartments().subscribe(d => this.departments = d);
    this.patientService.getPublicDoctors().subscribe(d => this.doctors = d);

    this.form.get('departmentId')?.valueChanges.subscribe(deptId => {
      this.filteredDoctors = this.doctors.filter(d => d.department.id === +deptId && d.available);
      this.form.get('doctorId')?.reset();
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';
    this.success = '';
    this.generatedToken = null;

    const { departmentId, ...payload } = this.form.value;

    this.receptionistService.generateToken(payload).subscribe({
      next: token => {
        this.generatedToken = token;
        this.success = `Token ${token.tokenNumber} generated successfully.`;
        this.form.reset({ priority: 'NORMAL' });
        this.loading = false;
      },
      error: err => {
        this.error = err.error?.message ?? 'Failed to generate token.';
        this.loading = false;
      }
    });
  }
}
