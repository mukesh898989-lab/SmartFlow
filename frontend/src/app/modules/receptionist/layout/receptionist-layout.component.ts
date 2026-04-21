import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-receptionist-layout',
  templateUrl: './receptionist-layout.component.html'
})
export class ReceptionistLayoutComponent {
  constructor(public auth: AuthService) {}
  logout(): void { this.auth.logout(); }
}
