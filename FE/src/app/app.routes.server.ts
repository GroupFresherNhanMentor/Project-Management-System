import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: 'projects/:id/edit', renderMode: RenderMode.Server },
  { path: 'projects/:id',      renderMode: RenderMode.Server },
  { path: 'projects',          renderMode: RenderMode.Server },
  { path: 'projects/new',      renderMode: RenderMode.Server },
  { path: 'members',           renderMode: RenderMode.Server },
  { path: 'members/new',       renderMode: RenderMode.Server },
  { path: 'sprints/:id',     renderMode: RenderMode.Server },
  { path: 'worklog/:userId', renderMode: RenderMode.Server },
  { path: 'tasks/:id',       renderMode: RenderMode.Server },
  { path: '**',              renderMode: RenderMode.Prerender },
];
