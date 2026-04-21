import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { Department, Doctor } from '../../../core/models';

@Component({
  selector: 'app-doctors',
  templateUrl: './doctors.component.html'
})
export class DoctorsComponent implements OnInit {

  doctors: Doctor[] = [];
  departments: Department[] = [];
  form: FormGroup;
  loading = false;
  success = '';
  error = '';

  constructor(private fb: FormBuilder, private adminService: AdminService) {
    this.form = this.fb.group({
      name:           ['', [Validators.required, Validators.minLength(2)]],
      email:          ['', [Validators.required, Validators.email]],
      password:       ['', [Validators.required, Validators.minLength(6)]],
      departmentId:   [null, Validators.required],
      specialization: ['']
    });
  }

  ngOnInit(): void {
    this.adminService.getDoctors().subscribe(d => this.doctors = d);
    this.adminService.getDepartments().subscribe(d => this.departments = d.filter(dep => dep.active));
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';
    this.success = '';

    this.adminService.createDoctor(this.form.value).subscribe({
      next: () => {
        this.success = 'Doctor created successfully.';
        this.form.reset();
        this.adminService.getDoctors().subscribe(d => this.doctors = d);
        this.loading = false;
      },
      error: err => {
        this.error = err.error?.message ?? 'Failed to create doctor.';
        this.loading = false;
      }
    });
  }
}
