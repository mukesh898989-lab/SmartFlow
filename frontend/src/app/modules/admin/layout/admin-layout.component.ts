import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  templateUrl: './admin-layout.component.html'
})
export class AdminLayoutComponent {
  constructor(public auth: AuthService) {}
  logout(): void { this.auth.logout(); }
}
