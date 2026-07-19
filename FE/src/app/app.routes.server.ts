import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: 'sprints/:id',     renderMode: RenderMode.Server },
  { path: 'worklog/:userId', renderMode: RenderMode.Server },
  { path: 'tasks/:id',       renderMode: RenderMode.Server },
  { path: '**',              renderMode: RenderMode.Prerender },
];
