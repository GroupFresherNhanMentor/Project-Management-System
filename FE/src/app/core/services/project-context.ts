import { Injectable, inject, signal, computed, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ProjectDto } from '../models/project.model';
import { ProjectRole } from '../models/api.model';

const STORAGE_KEY = 'pms_selected_project';

@Injectable({ providedIn: 'root' })
export class ProjectContextService {
  private readonly platformId = inject(PLATFORM_ID);

  private readonly _projects = signal<ProjectDto[]>([]);
  private readonly _selectedId = signal<string | null>(this.loadStoredId());
  private readonly _currentUserRole = signal<ProjectRole | null>(null);

  readonly projects = this._projects.asReadonly();
  readonly currentUserProjectRole = this._currentUserRole.asReadonly();

  readonly selectedProject = computed(() =>
    this._projects().find(p => p.id === this._selectedId()) ?? this._projects()[0] ?? null,
  );

  readonly selectedProjectId = computed(() => this.selectedProject()?.id ?? null);

  setProjects(projects: ProjectDto[]): void {
    this._projects.set(projects);
    const storedId = this.loadStoredId();
    const valid = storedId && projects.some(p => p.id === storedId);
    if (!valid && projects[0]) {
      this.selectProject(projects[0].id);
    }
  }

  selectProject(id: string): void {
    this._selectedId.set(id);
    this._currentUserRole.set(null);
    if (this.isBrowser()) globalThis.localStorage.setItem(STORAGE_KEY, id);
  }

  setCurrentUserRole(role: ProjectRole | null): void {
    this._currentUserRole.set(role);
  }

  private loadStoredId(): string | null {
    return this.isBrowser() ? (globalThis.localStorage.getItem(STORAGE_KEY) ?? null) : null;
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId) && typeof globalThis.localStorage !== 'undefined';
  }
}
