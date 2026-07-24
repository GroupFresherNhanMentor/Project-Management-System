import { Component, inject, OnInit, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ProjectStatus } from '../../../../core/models/api.model';
import { ProjectService } from '../../../../core/services/project';
import { ToastService } from '../../../../core/services/toast';

@Component({
  selector: 'app-project-edit',
  imports: [FormsModule, RouterLink],
  templateUrl: './project-edit.html',
})
export class ProjectEdit implements OnInit {
  private readonly route          = inject(ActivatedRoute);
  private readonly router         = inject(Router);
  private readonly projectService = inject(ProjectService);
  private readonly toast          = inject(ToastService);
  private readonly platformId     = inject(PLATFORM_ID);

  readonly projectId = this.route.snapshot.paramMap.get('id') ?? '';
  readonly statuses: ProjectStatus[] = ['PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED'];
  projectCode = '';
  projectName = '';
  description = '';
  startDate = '';
  endDate = '';
  status: ProjectStatus = 'PLANNING';
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    if (!this.projectId) {
      this.errorMessage.set('Missing project ID.');
      this.loading.set(false);
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.projectService.getProjectById(this.projectId).subscribe({
      next: project => {
        this.projectCode = project.projectCode;
        this.projectName = project.projectName;
        this.description = project.description ?? '';
        this.startDate = project.startDate;
        this.endDate = project.endDate;
        this.status = project.status;
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        const message = error.error?.message ?? 'Unable to load project.';
        this.errorMessage.set(message);
        this.toast.error(message);
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    if (!this.projectName.trim() || !this.startDate || !this.endDate) {
      this.toast.error('Please fill in all required fields.'); return;
    }
    if (this.endDate < this.startDate) {
      this.toast.error('End date must not be before start date.'); return;
    }
    this.submitting.set(true);
    this.projectService.updateProject(this.projectId, {
      projectName: this.projectName.trim(),
      description: this.description.trim() || undefined,
      startDate: this.startDate,
      endDate: this.endDate,
      status: this.status,
    }).pipe(finalize(() => this.submitting.set(false))).subscribe({
      next: () => {
        this.toast.success('Project updated.');
        void this.router.navigate(['/projects', this.projectId]);
      },
      error: (error: HttpErrorResponse) =>
        this.toast.error(error.error?.message ?? 'Unable to update project.'),
    });
  }
}
