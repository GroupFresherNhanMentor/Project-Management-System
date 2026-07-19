import { Component, inject } from '@angular/core';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-toast',
  template: `
    <div class="fixed bottom-5 right-5 z-[200] flex flex-col gap-2">
      @for (t of toastService.toasts(); track t.id) {
        <div
          class="toast-enter flex min-w-[260px] max-w-xs items-center justify-between gap-3 rounded-[9px] px-4 py-3 text-[13px] font-medium shadow-[0_10px_30px_-8px_rgba(20,24,40,0.25)]"
          [class]="t.type === 'success'
            ? 'bg-[#14161C] text-white'
            : 'bg-[#FDECEC] text-[#D0342C]'"
        >
          <span>{{ t.message }}</span>
          <button (click)="toastService.dismiss(t.id)" class="opacity-60 hover:opacity-100 text-lg leading-none">&times;</button>
        </div>
      }
    </div>
  `,
})
export class ToastContainer {
  readonly toastService = inject(ToastService);
}
