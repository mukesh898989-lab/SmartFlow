import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { PatientService } from '../../../core/services/patient.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { QueuePositionResponse, Token } from '../../../core/models';

@Component({
  selector: 'app-track-token',
  templateUrl: './track-token.component.html'
})
export class TrackTokenComponent implements OnInit, OnDestroy {

  tokenNumber = '';
  inputToken = '';
  positionData: QueuePositionResponse | null = null;

  loading = false;
  error = '';

  private wsSub: Subscription | null = null;
  private pollSub: Subscription | null = null;

  constructor(
    private route: ActivatedRoute,
    private patientService: PatientService,
    private ws: WebSocketService
  ) {}

  ngOnInit(): void {
    // Support direct link with ?token=XXX
    this.route.queryParams.subscribe(params => {
      if (params['token']) {
        this.inputToken = params['token'];
        this.trackToken();
      }
    });
  }

  trackToken(): void {
    const token = this.inputToken.trim().toUpperCase();
    if (!token) return;

    this.loading = true;
    this.error = '';
    this.tokenNumber = token;

    this.patientService.getQueuePosition(token).subscribe({
      next: data => {
        this.positionData = data;
        this.loading = false;
        this.startRealTimeUpdates(token);
      },
      error: err => {
        this.error = err.error?.message ?? 'Token not found. Please check the number.';
        this.loading = false;
        this.positionData = null;
      }
    });
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'WAITING':     return 'Waiting';
      case 'IN_PROGRESS': return 'In Consultation';
      case 'COMPLETED':   return 'Completed';
      default:            return status;
    }
  }

  getStatusClass(status: string): string {
    return `badge badge-${status.toLowerCase().replace('_', '-')}`;
  }

  private startRealTimeUpdates(tokenNumber: string): void {
    // Clean up previous subscriptions
    this.wsSub?.unsubscribe();
    this.pollSub?.unsubscribe();

    // Real-time via WebSocket
    this.wsSub = this.ws.subscribeToToken<Token>(tokenNumber).subscribe(updated => {
      if (this.positionData) {
        this.positionData.status = updated.status;
        this.positionData.position = updated.queuePosition;
      }
    });

    // Fallback polling every 15 seconds
    this.pollSub = interval(15000).pipe(
      switchMap(() => this.patientService.getQueuePosition(tokenNumber))
    ).subscribe(data => {
      this.positionData = data;
    });
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
    this.pollSub?.unsubscribe();
  }
}
