import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {

  constructor(private auth: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const expectedRoles: Role[] = route.data['roles'];
    const userRole = this.auth.getRole();

    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/auth/login']);
      return false;
    }

    if (expectedRoles && userRole && expectedRoles.includes(userRole)) {
      return true;
    }

    // Redirect to role-specific dashboard
    this.redirectToDashboard(userRole);
    return false;
  }

  private redirectToDashboard(role: Role | null): void {
    switch (role) {
      case 'ADMIN':        this.router.navigate(['/admin']);        break;
      case 'RECEPTIONIST': this.router.navigate(['/receptionist']); break;
      case 'DOCTOR':       this.router.navigate(['/doctor']);       break;
      default:             this.router.navigate(['/auth/login']);   break;
    }
  }
}
