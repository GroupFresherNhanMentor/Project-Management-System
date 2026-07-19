import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LoadingBar } from './shared/components/loading-bar/loading-bar';
import { ToastContainer } from './shared/components/toast/toast';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, LoadingBar, ToastContainer],
  template: `
    <app-loading-bar />
    <app-toast />
    <router-outlet />
  `,
})
export class App {}
