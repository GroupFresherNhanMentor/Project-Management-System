import { Component, inject, signal, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProjectDto } from '../../../../core/models/project.model';
import { AuthService } from '../../../../core/services/auth';
import { ProjectService } from '../../../../core/services/project';

@Component({
  selector: 'app-project-detail',
  imports: [RouterLink],
  templateUrl: './project-detail.html',
})
export class ProjectDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly projectService = inject(ProjectService);
  private readonly authService = inject(AuthService);

  readonly project = signal<ProjectDto | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly isAdmin = this.authService.getCurrentUser()?.role === 'ADMIN';
  readonly projectId = this.route.snapshot.paramMap.get('id') ?? '';

  ngOnInit(): void {
    this.projectService.getProjectById(this.projectId).subscribe({
      next: project => { this.project.set(project); this.loading.set(false); },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(error.error?.message ?? 'Unable to load project.');
        this.loading.set(false);
      },
    });
  }

  edit(): void { void this.router.navigate(['/projects', this.projectId, 'edit']); }
  members(): void { void this.router.navigate(['/members'], { queryParams: { projectId: this.projectId } }); }
}
