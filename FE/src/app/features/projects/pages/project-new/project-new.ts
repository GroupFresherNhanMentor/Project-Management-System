import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { ToastService } from '../../../../core/services/toast';

@Component({
  selector: 'app-project-new',
  imports: [FormsModule, RouterLink],
  templateUrl: './project-new.html',
})
export class ProjectNew {
  private readonly toast  = inject(ToastService);
  private readonly router = inject(Router);

  projectCode = '';
  projectName = '';
  description = '';
  startDate   = '';
  endDate     = '';
  submitting  = false;

  submit(): void {
    if (!this.projectCode || !this.projectName || !this.startDate || !this.endDate) {
      this.toast.error('Please fill in all required fields.'); return;
    }
    this.submitting = true;
    this.toast.success('Project created.');
    void this.router.navigate(['/projects']);
  }
}
