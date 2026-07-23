import { Component, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { CreateSprintRequest } from '../../../../core/models/sprint.model';
import { ProjectService } from '../../../../core/services/project';
import { ToastService } from '../../../../core/services/toast';

@Component({
  selector: 'app-sprint-new',
  imports: [FormsModule, RouterLink],
  templateUrl: './sprint-new.html',
})
export class SprintNew {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly projectService = inject(ProjectService);
  private readonly toast = inject(ToastService);

  projectId: string = '';
  sprintName = ''; goal = ''; startDate = ''; endDate = ''; submitting = false;

  constructor() {
    let r: ActivatedRoute | null = this.route;
    while (r) {
      const id = r.snapshot.paramMap.get('id');
      if (id) { this.projectId = id; break; }
      r = r.parent;
    }
  }

  submit(): void {
    if (!this.projectId || !this.sprintName || !this.startDate || !this.endDate) {
      this.toast.error('Please fill in all required fields.');
      return;
    }
    this.submitting = true;
    const body: CreateSprintRequest = {
      sprintName: this.sprintName,
      goal: this.goal || undefined,
      startDate: this.startDate,
      endDate: this.endDate,
    };
    this.projectService.createSprint(this.projectId, body)
      .pipe(finalize(() => this.submitting = false))
      .subscribe({
        next: () => {
          this.toast.success('Sprint created successfully.');
          void this.router.navigate(['../'], { relativeTo: this.route });
        },
        error: (err: HttpErrorResponse) => this.toast.error(err.error?.message ?? 'Unable to create sprint.'),
      });
  }

  cancel(): void {
    void this.router.navigate(['../'], { relativeTo: this.route });
  }
}
