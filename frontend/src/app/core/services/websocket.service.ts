import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Client, StompSubscription } from '@stomp/stompjs';
import { environment } from '../../../environments/environment';

// SockJS loaded via require to avoid esModuleInterop issues
declare const require: any;

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {

  private client: Client;
  private connected$ = new Subject<void>();
  private isConnected = false;
  private pendingSubscriptions: Array<() => void> = [];

  constructor() {
    this.client = new Client({
      webSocketFactory: () => {
        const SockJS = require('sockjs-client');
        return new SockJS(environment.wsUrl);
      },
      reconnectDelay: 5000,
      onConnect: () => {
        this.isConnected = true;
        // Execute any subscriptions that were waiting for connection
        this.pendingSubscriptions.forEach(fn => fn());
        this.pendingSubscriptions = [];
      },
      onDisconnect: () => {
        this.isConnected = false;
      }
    });
    this.client.activate();
  }

  /**
   * Subscribe to real-time queue updates for a specific doctor.
   * Destination: /topic/queue/{doctorId}
   */
  subscribeToQueue<T>(doctorId: number): Observable<T> {
    return this.subscribeTo<T>(`/topic/queue/${doctorId}`);
  }

  /**
   * Subscribe to real-time status updates for a specific token.
   * Destination: /topic/token/{tokenNumber}
   */
  subscribeToToken<T>(tokenNumber: string): Observable<T> {
    return this.subscribeTo<T>(`/topic/token/${tokenNumber}`);
  }

  private subscribeTo<T>(destination: string): Observable<T> {
    const subject = new Subject<T>();
    let stompSub: StompSubscription | null = null;

    const doSubscribe = () => {
      stompSub = this.client.subscribe(destination, message => {
        try {
          subject.next(JSON.parse(message.body) as T);
        } catch {
          // ignore malformed messages
        }
      });
    };

    // Return an Observable that cleans up the STOMP subscription on unsubscribe
    return new Observable<T>(observer => {
      const innerSub = subject.subscribe(observer);

      if (this.isConnected) {
        doSubscribe();
      } else {
        this.pendingSubscriptions.push(doSubscribe);
      }

      // Cleanup when Angular unsubscribes
      return () => {
        innerSub.unsubscribe();
        stompSub?.unsubscribe();
      };
    });
  }

  ngOnDestroy(): void {
    this.client.deactivate();
  }
}
