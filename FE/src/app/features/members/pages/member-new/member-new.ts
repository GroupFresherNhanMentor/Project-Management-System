import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ProjectRole } from '../../../../core/models/api.model';
import { ProjectMemberCandidateDto } from '../../../../core/models/project-member.model';
import { ProjectService } from '../../../../core/services/project';
import { ProjectContextService } from '../../../../core/services/project-context';
import { ToastService } from '../../../../core/services/toast';

@Component({
  selector: 'app-member-new',
  imports: [FormsModule],
  templateUrl: './member-new.html',
})
export class MemberNew implements OnInit {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly projectService = inject(ProjectService);
  private readonly projectContext = inject(ProjectContextService);
  private readonly toast = inject(ToastService);

  readonly roles: ProjectRole[] = ['PM', 'DEV', 'TESTER'];
  readonly projectId = signal<string | null>(
    this.route.snapshot.queryParamMap.get('projectId')
      ?? this.projectContext.selectedProjectId(),
  );
  readonly candidates = signal<ProjectMemberCandidateDto[]>([]);
  readonly loadingUsers = signal(false);
  readonly userLoadError = signal<string | null>(null);
  readonly candidatePage = signal(0);
  readonly candidateTotalPages = signal(0);
  readonly candidateTotalElements = signal(0);
  readonly candidatePageSize = 10;

  userId = '';
  candidateKeyword = '';
  role: ProjectRole = 'DEV';
  readonly submitting = signal(false);

  ngOnInit(): void {
    this.loadCandidates();
  }

  loadCandidates(page = this.candidatePage()): void {
    const projectId = this.projectId();
    if (!projectId) {
      this.userLoadError.set('No project selected.');
      return;
    }

    this.loadingUsers.set(true);
    this.userLoadError.set(null);
    this.projectService.getMemberCandidates(
      projectId,
      page,
      this.candidatePageSize,
      this.candidateKeyword.trim() || undefined,
    )
      .pipe(finalize(() => this.loadingUsers.set(false)))
      .subscribe({
        next: result => {
          this.candidates.set(result.items);
          this.candidatePage.set(result.pageNumber);
          this.candidateTotalPages.set(result.totalPages);
          this.candidateTotalElements.set(result.totalElements);
          if (!result.items.some(candidate => candidate.id === this.userId)) {
            this.userId = '';
          }
        },
        error: (error: HttpErrorResponse) => {
          this.candidates.set([]);
          this.userLoadError.set(error.error?.message ?? 'Unable to load available users.');
        },
      });
  }

  applyCandidateSearch(): void {
    this.loadCandidates(0);
  }

  previousCandidatePage(): void {
    if (this.candidatePage() > 0) {
      this.loadCandidates(this.candidatePage() - 1);
    }
  }

  nextCandidatePage(): void {
    if (this.candidatePage() + 1 < this.candidateTotalPages()) {
      this.loadCandidates(this.candidatePage() + 1);
    }
  }

  submit(): void {
    const projectId = this.projectId();
    if (!projectId || !this.userId.trim()) {
      this.toast.error('Please select a user.'); return;
    }
    this.submitting.set(true);
    this.projectService.addMember(projectId, {
      userId: this.userId.trim(),
      projectRole: this.role,
    }).pipe(finalize(() => this.submitting.set(false))).subscribe({
      next: () => {
        this.toast.success('Member added.');
        void this.router.navigate(['/members'], { queryParams: { projectId } });
      },
      error: (error: HttpErrorResponse) =>
        this.toast.error(error.error?.message ?? 'Unable to add member.'),
    });
  }

  cancel(): void {
    void this.router.navigate(['/members'], { queryParams: { projectId: this.projectId() } });
  }
}
