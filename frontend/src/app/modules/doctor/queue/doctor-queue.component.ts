import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { DoctorService } from '../../../core/services/doctor.service';
import { AuthService } from '../../../core/services/auth.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { Token, TokenStatus } from '../../../core/models';

@Component({
  selector: 'app-doctor-queue',
  templateUrl: './doctor-queue.component.html'
})
export class DoctorQueueComponent implements OnInit, OnDestroy {

  queue: Token[] = [];
  currentToken: Token | null = null;
  loading = false;
  actionLoading = false;
  error = '';
  success = '';

  private wsSub: Subscription | null = null;

  constructor(
    private doctorService: DoctorService,
    private auth: AuthService,
    private ws: WebSocketService
  ) {}

  ngOnInit(): void {
    this.loadQueue();
  }

  loadQueue(): void {
    this.loading = true;
    this.doctorService.getMyQueue().subscribe({
      next: tokens => {
        this.processQueue(tokens);
        this.loading = false;
        this.subscribeToRealTimeUpdates();
      },
      error: () => { this.loading = false; }
    });
  }

  callNext(): void {
    this.actionLoading = true;
    this.error = '';
    this.doctorService.callNextPatient().subscribe({
      next: token => {
        this.success = `Now serving: ${token.patientName} (${token.tokenNumber})`;
        this.loadQueue();
        this.actionLoading = false;
        setTimeout(() => this.success = '', 4000);
      },
      error: err => {
        this.error = err.error?.message ?? 'No patients in queue.';
        this.actionLoading = false;
      }
    });
  }

  completeCurrentToken(): void {
    if (!this.currentToken) return;
    this.actionLoading = true;

    this.doctorService.updateTokenStatus(this.currentToken.id, { status: 'COMPLETED' as TokenStatus }).subscribe({
      next: () => {
        this.success = 'Consultation completed.';
        this.currentToken = null;
        this.loadQueue();
        this.actionLoading = false;
        setTimeout(() => this.success = '', 4000);
      },
      error: err => {
        this.error = err.error?.message ?? 'Failed to complete.';
        this.actionLoading = false;
      }
    });
  }

  getPriorityClass(p: string): string {
    return `badge badge-${p.toLowerCase()}`;
  }

  private processQueue(tokens: Token[]): void {
    this.currentToken = tokens.find(t => t.status === 'IN_PROGRESS') ?? null;
    this.queue = tokens.filter(t => t.status === 'WAITING');
  }

  private subscribeToRealTimeUpdates(): void {
    const user = this.auth.getCurrentUser();
    if (!user) return;

    // Use doctorId from the first token if available, or fall back to userId
    const doctorId = this.currentToken?.doctorId ?? this.queue[0]?.doctorId;
    if (doctorId == null) return;

    this.wsSub?.unsubscribe();
    this.wsSub = this.ws.subscribeToQueue<Token[]>(doctorId).subscribe(updated => {
      // Queue broadcasts contain only WAITING tokens — update the waiting list only.
      // currentToken is managed exclusively by explicit doctor actions (callNext / complete).
      this.queue = updated.filter(t => t.status === 'WAITING');
    });
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
  }
}
