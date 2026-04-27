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
  skippedQueue: Token[] = [];
  loading = false;
  skippedLoading = false;
  error = '';
  success = '';

  priorities: Priority[] = ['NORMAL', 'URGENT', 'EMERGENCY'];
  private wsSubs: Subscription[] = [];

  constructor(
    private receptionistService: ReceptionistService,
    private ws: WebSocketService
  ) {}

  ngOnInit(): void {
    this.loadQueue();
    this.loadSkippedQueue();
  }

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

  loadSkippedQueue(): void {
    this.skippedLoading = true;
    this.receptionistService.getSkippedQueue().subscribe({
      next: tokens => { this.skippedQueue = tokens; this.skippedLoading = false; },
      error: () => { this.skippedLoading = false; }
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

  recallToken(token: Token): void {
    if (!confirm(`Recall ${token.tokenNumber} – ${token.patientName}? They will be placed as the next patient after the current consultation.`)) return;
    this.receptionistService.recallToken(token.id).subscribe({
      next: recalled => {
        this.skippedQueue = this.skippedQueue.filter(t => t.id !== recalled.id);
        this.success = `${recalled.tokenNumber} recalled — placed as next in line.`;
        this.loadQueue();
        setTimeout(() => this.success = '', 5000);
      },
      error: err => { this.error = err.error?.message ?? 'Failed to recall token.'; }
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
      // Preserve IN_PROGRESS tokens, replace WAITING tokens from broadcast
      this.queue = [
        ...this.queue.filter(t => t.doctorId !== doctorId || t.status === 'IN_PROGRESS'),
        ...updatedQueue
      ];
      // Refresh skipped list whenever queue changes (a skip may have just occurred)
      this.loadSkippedQueue();
    });
    this.wsSubs.push(sub);
  }

  ngOnDestroy(): void {
    this.wsSubs.forEach(s => s.unsubscribe());
  }
}
