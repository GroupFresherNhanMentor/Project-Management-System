import { Component, inject } from '@angular/core';
import { LoadingService } from '../../../core/services/loading';

@Component({
  selector: 'app-loading-bar',
  template: `
    @if (loadingService.isLoading()) {
      <div class="fixed inset-x-0 top-0 z-50 h-0.5 overflow-hidden bg-slate-200">
        <div class="loading-bar h-full bg-indigo-500"></div>
      </div>
    }
  `,
})
export class LoadingBar {
  readonly loadingService = inject(LoadingService);
}
