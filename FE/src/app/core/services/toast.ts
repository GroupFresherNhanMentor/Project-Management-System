import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: string;
  type: 'success' | 'error';
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly _toasts = signal<Toast[]>([]);
  readonly toasts = this._toasts.asReadonly();

  success(message: string): void { this.push({ type: 'success', message }); }
  error(message: string): void   { this.push({ type: 'error',   message }); }

  dismiss(id: string): void {
    this._toasts.update(t => t.filter(x => x.id !== id));
  }

  private push(toast: Omit<Toast, 'id'>): void {
    const id = crypto.randomUUID();
    this._toasts.update(t => [...t, { ...toast, id }]);
    setTimeout(() => this.dismiss(id), 2600);
  }
}
