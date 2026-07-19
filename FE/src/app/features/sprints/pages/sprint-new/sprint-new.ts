import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';

@Component({
  selector: 'app-sprint-new',
  imports: [FormsModule, RouterLink],
  templateUrl: './sprint-new.html',
})
export class SprintNew {
  private readonly router = inject(Router);
  sprintName = ''; goal = ''; startDate = ''; endDate = ''; submitting = false;

  submit(): void { void this.router.navigate(['/sprints']); }
  cancel(): void { void this.router.navigate(['/sprints']); }
}
