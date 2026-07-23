import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { ToastService } from '../../../../core/services/toast';
import { ProjectService } from '../../../../core/services/project';
import { ProjectStatus } from '../../../../core/models/api.model';

@Component({
  selector: 'app-project-new',
  imports: [FormsModule, RouterLink],
  templateUrl: './project-new.html',
})
export class ProjectNew {
  private readonly toast  = inject(ToastService);
  private readonly router = inject(Router);
  private readonly projectService = inject(ProjectService);

  projectCode = '';
  projectName = '';
  description = '';
  startDate   = '';
  endDate     = '';
  status: ProjectStatus = 'PLANNING';
  readonly statuses: ProjectStatus[] = ['PLANNING', 'ACTIVE', 'ON_HOLD'];
  submitting  = false;

  submit(): void {
    if (!this.projectCode || !this.projectName || !this.startDate || !this.endDate) {
      this.toast.error('Please fill in all required fields.'); return;
    }
    if (this.endDate < this.startDate) {
      this.toast.error('End date must not be before start date.'); return;
    }
    this.submitting = true;
    this.projectService.createProject({
      projectCode: this.projectCode.trim(),
      projectName: this.projectName.trim(),
      description: this.description.trim() || undefined,
      startDate: this.startDate,
      endDate: this.endDate,
      status: this.status,
    }).pipe(finalize(() => this.submitting = false)).subscribe({
      next: project => {
        this.toast.success('Project created.');
        void this.router.navigate(['/projects', project.id]);
      },
      error: (error: HttpErrorResponse) =>
        this.toast.error(error.error?.message ?? 'Unable to create project.'),
    });
  }
}
