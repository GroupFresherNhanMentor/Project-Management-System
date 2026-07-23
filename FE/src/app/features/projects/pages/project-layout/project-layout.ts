import { Component, inject, signal, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../../core/services/auth';
import { ProjectService } from '../../../../core/services/project';
import { ProjectDto } from '../../../../core/models/project.model';

@Component({
  selector: 'app-project-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './project-layout.html',
})
export class ProjectLayout implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly projectService = inject(ProjectService);
  private readonly authService = inject(AuthService);

  readonly project = signal<ProjectDto | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly isAdmin = this.authService.getCurrentUser()?.role === 'ADMIN';

  readonly projectId: string = this.route.snapshot.paramMap.get('id') ?? '';
  readonly tabs = [
    { label: 'Board',   path: 'board' },
    { label: 'Backlog', path: 'backlog' },
    { label: 'Sprints', path: 'sprints' },
    { label: 'Members', path: 'members' },
    { label: 'Worklog Report', path: 'worklog' },
  ];

  ngOnInit(): void {
    this.projectService.getProjectById(this.projectId).subscribe({
      next: p => { this.project.set(p); this.loading.set(false); },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(err.error?.message ?? 'Unable to load project.');
        this.loading.set(false);
      },
    });
  }
}
