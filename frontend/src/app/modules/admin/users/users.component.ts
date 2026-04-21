import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { User } from '../../../core/models';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {

  users: User[] = [];
  form: FormGroup;
  loading = false;
  success = '';
  error = '';

  constructor(private fb: FormBuilder, private adminService: AdminService) {
    this.form = this.fb.group({
      name:     ['', [Validators.required, Validators.minLength(2)]],
      email:    ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role:     ['RECEPTIONIST', Validators.required]
    });
  }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.adminService.getUsers().subscribe(u => this.users = u);
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';
    this.success = '';

    this.adminService.createUser(this.form.value).subscribe({
      next: () => {
        this.success = 'User created successfully.';
        this.form.reset({ role: 'RECEPTIONIST' });
        this.load();
        this.loading = false;
      },
      error: err => {
        this.error = err.error?.message ?? 'Failed to create user.';
        this.loading = false;
      }
    });
  }

  toggleStatus(user: User): void {
    const action = user.active
      ? this.adminService.deactivateUser(user.id)
      : this.adminService.activateUser(user.id);
    action.subscribe(() => this.load());
  }
}
