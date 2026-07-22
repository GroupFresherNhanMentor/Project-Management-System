import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: 'projects/new',              renderMode: RenderMode.Server },
  { path: 'projects/:id',              renderMode: RenderMode.Server },
  { path: 'projects/:id/edit',         renderMode: RenderMode.Server },
  { path: 'projects/:id/board',        renderMode: RenderMode.Server },
  { path: 'projects/:id/backlog',      renderMode: RenderMode.Server },
  { path: 'projects/:id/sprints',      renderMode: RenderMode.Server },
  { path: 'projects/:id/sprints/new',  renderMode: RenderMode.Server },
  { path: 'projects/:id/sprints/:sprintId', renderMode: RenderMode.Server },
  { path: 'projects/:id/members',      renderMode: RenderMode.Server },
  { path: 'projects/:id/members/new',  renderMode: RenderMode.Server },
  { path: 'projects/:id/worklog',      renderMode: RenderMode.Server },
  { path: 'projects/:id/worklog/:userId', renderMode: RenderMode.Server },
  { path: 'tasks/:id',                 renderMode: RenderMode.Server },
  { path: '**',                        renderMode: RenderMode.Prerender },
];
