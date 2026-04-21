import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';
import { Token } from '../../../core/models';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {

  activeTokens: Token[] = [];
  loading = false;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadQueue();
  }

  loadQueue(): void {
    this.loading = true;
    this.adminService.getAllActiveTokens().subscribe({
      next: tokens => { this.activeTokens = tokens; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  getPriorityClass(priority: string): string {
    return `badge badge-${priority.toLowerCase()}`;
  }

  getStatusClass(status: string): string {
    return `badge badge-${status.toLowerCase().replace('_', '-')}`;
  }
}
