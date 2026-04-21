import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-doctor-layout',
  templateUrl: './doctor-layout.component.html'
})
export class DoctorLayoutComponent {
  constructor(public auth: AuthService) {}
  logout(): void { this.auth.logout(); }
}
