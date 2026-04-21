import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { Department } from '../../../core/models';

@Component({
  selector: 'app-departments',
  templateUrl: './departments.component.html'
})
export class DepartmentsComponent implements OnInit {

  departments: Department[] = [];
  form: FormGroup;
  loading = false;
  success = '';
  error = '';

  constructor(private fb: FormBuilder, private adminService: AdminService) {
    this.form = this.fb.group({
      name:        ['', [Validators.required, Validators.minLength(2)]],
      description: ['']
    });
  }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.adminService.getDepartments().subscribe(d => this.departments = d);
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';
    this.success = '';

    this.adminService.createDepartment(this.form.value).subscribe({
      next: () => {
        this.success = 'Department created successfully.';
        this.form.reset();
        this.load();
        this.loading = false;
      },
      error: err => {
        this.error = err.error?.message ?? 'Failed to create department.';
        this.loading = false;
      }
    });
  }
}
