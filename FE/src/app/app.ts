import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LoadingBar } from './shared/components/loading-bar/loading-bar';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, LoadingBar],
  template: `
    <app-loading-bar />
    <router-outlet />
  `,
})
export class App {}
