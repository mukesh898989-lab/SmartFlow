import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {

  form: FormGroup;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email:    ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    // Already logged in → redirect
    if (this.auth.isLoggedIn()) {
      this.redirectByRole();
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';

    this.auth.login(this.form.value).subscribe({
      next: () => this.redirectByRole(),
      error: err => {
        this.error = err.error?.message ?? 'Invalid email or password';
        this.loading = false;
      }
    });
  }

  private redirectByRole(): void {
    const role = this.auth.getRole();
    switch (role) {
      case 'ADMIN':        this.router.navigate(['/admin']);        break;
      case 'RECEPTIONIST': this.router.navigate(['/receptionist']); break;
      case 'DOCTOR':       this.router.navigate(['/doctor']);       break;
    }
  }
}
