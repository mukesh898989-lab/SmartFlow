import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { ReceptionistService } from '../../../core/services/receptionist.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { Token, Priority } from '../../../core/models';

@Component({
  selector: 'app-queue-manage',
  templateUrl: './queue-manage.component.html'
})
export class QueueManageComponent implements OnInit, OnDestroy {

  queue: Token[] = [];
  loading = false;
  error = '';
  success = '';

  priorities: Priority[] = ['NORMAL', 'URGENT', 'EMERGENCY'];
  private wsSubs: Subscription[] = [];

  constructor(
    private receptionistService: ReceptionistService,
    private ws: WebSocketService
  ) {}

  ngOnInit(): void { this.loadQueue(); }

  loadQueue(): void {
    this.wsSubs.forEach(s => s.unsubscribe());
    this.wsSubs = [];
    this.loading = true;
    this.receptionistService.getActiveQueue().subscribe({
      next: tokens => {
        this.queue = tokens;
        this.loading = false;
        const doctorIds = [...new Set(tokens.map(t => t.doctorId))];
        doctorIds.forEach(id => this.subscribeToDoctor(id));
      },
      error: () => { this.loading = false; }
    });
  }

  updatePriority(token: Token, newPriority: Priority): void {
    this.receptionistService.updatePriority(token.id, { priority: newPriority }).subscribe({
      next: updated => {
        const idx = this.queue.findIndex(t => t.id === updated.id);
        if (idx >= 0) this.queue[idx] = updated;
        this.success = `Priority updated to ${newPriority}`;
        setTimeout(() => this.success = '', 3000);
      },
      error: err => { this.error = err.error?.message ?? 'Failed to update priority.'; }
    });
  }

  removeToken(token: Token): void {
    if (!confirm(`Remove ${token.tokenNumber} from queue?`)) return;
    this.receptionistService.removeToken(token.id).subscribe({
      next: () => {
        this.queue = this.queue.filter(t => t.id !== token.id);
        this.success = 'Token removed from queue.';
        setTimeout(() => this.success = '', 3000);
      },
      error: err => { this.error = err.error?.message ?? 'Failed to remove token.'; }
    });
  }

  getPriorityClass(p: string): string {
    return `badge badge-${p.toLowerCase()}`;
  }

  getStatusClass(s: string): string {
    return `badge badge-${s.toLowerCase().replace('_', '-')}`;
  }

  private subscribeToDoctor(doctorId: number): void {
    const sub = this.ws.subscribeToQueue<Token[]>(doctorId).subscribe(updatedQueue => {
      // Queue broadcasts contain only WAITING tokens — preserve IN_PROGRESS tokens when merging.
      this.queue = [
        ...this.queue.filter(t => t.doctorId !== doctorId || t.status === 'IN_PROGRESS'),
        ...updatedQueue
      ];
    });
    this.wsSubs.push(sub);
  }

  ngOnDestroy(): void {
    this.wsSubs.forEach(s => s.unsubscribe());
  }
}
