import { Component, signal, inject, effect } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ProjectMemberDto } from '../../../../core/models/project-member.model';
import { ProjectRole } from '../../../../core/models/api.model';
import { AuthService } from '../../../../core/services/auth';
import { ProjectService } from '../../../../core/services/project';
import { ProjectContextService } from '../../../../core/services/project-context';
import { ToastService } from '../../../../core/services/toast';
import { InitialsPipe } from '../../../../shared/pipes/initials.pipe';

@Component({
  selector: 'app-member-list',
  imports: [FormsModule, InitialsPipe],
  templateUrl: './member-list.html',
})
export class MemberList {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly projectService = inject(ProjectService);
  private readonly projectContext = inject(ProjectContextService);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);
  private loadedProjectId: string | null = null;

  readonly members = signal<ProjectMemberDto[]>([]);
  readonly projectId = signal<string | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly canManage = signal(this.authService.getCurrentUser()?.role === 'ADMIN');
  readonly currentProjectRole = signal<ProjectRole | null>(null);
  readonly pendingRemoval = signal<ProjectMemberDto | null>(null);
  readonly removingMemberId = signal<string | null>(null);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly pageSize = 10;
  keyword = '';

  constructor() {
    effect(() => {
      const id = this.route.snapshot.queryParamMap.get('projectId')
        ?? this.projectContext.selectedProjectId();
      if (id && id !== this.loadedProjectId) {
        this.loadedProjectId = id;
        this.projectId.set(id);
        this.keyword = '';
        this.resolveCurrentMembership(id);
        this.load(id, 0);
      }
    });
  }

  load(projectId = this.projectId(), page = this.page()): void {
    if (!projectId) return;
    this.loading.set(true);
    this.errorMessage.set(null);
    this.projectService.getMembers(
      projectId,
      page,
      this.pageSize,
      this.keyword.trim() || undefined,
    ).subscribe({
      next: result => {
        this.members.set(result.items);
        this.page.set(result.pageNumber);
        this.totalPages.set(result.totalPages);
        this.totalElements.set(result.totalElements);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(error.error?.message ?? 'Unable to load project members.');
        this.loading.set(false);
      },
    });
  }

  applySearch(): void {
    this.load(this.projectId(), 0);
  }

  previousPage(): void {
    if (this.page() > 0) this.load(this.projectId(), this.page() - 1);
  }

  nextPage(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.load(this.projectId(), this.page() + 1);
    }
  }

  newMember(): void {
    void this.router.navigate(['/members/new'], { queryParams: { projectId: this.projectId() } });
  }

  removeMember(member: ProjectMemberDto): void {
    if (!this.projectId() || !this.canRemove(member)) return;
    this.pendingRemoval.set(member);
  }

  cancelRemoval(): void {
    if (this.removingMemberId()) return;
    this.pendingRemoval.set(null);
  }

  confirmRemoval(): void {
    const projectId = this.projectId();
    const member = this.pendingRemoval();
    if (!projectId || !member || !this.canRemove(member)) {
      this.pendingRemoval.set(null);
      return;
    }

    this.removingMemberId.set(member.id);

    this.projectService.removeMember(projectId, member.id)
      .pipe(finalize(() => this.removingMemberId.set(null)))
      .subscribe({
      next: () => {
        this.pendingRemoval.set(null);
        this.toast.success('Member removed.');
        this.load(projectId);
      },
      error: (error: HttpErrorResponse) =>
        this.toast.error(error.error?.message ?? 'Unable to remove member.'),
      });
  }

  canRemove(member: ProjectMemberDto): boolean {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser || !this.canManage() || member.status !== 'ACTIVE') return false;
    if (member.userId === currentUser.id || member.systemRole === 'ADMIN') return false;
    return currentUser.role === 'ADMIN' || (
      this.currentProjectRole() === 'PM' && member.projectRole !== 'PM'
    );
  }

  statusBadge(s: string): string {
    return s === 'ACTIVE' ? 'badge badge-active' : 'badge badge-locked';
  }

  private resolveCurrentMembership(projectId: string): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser?.role === 'ADMIN') {
      this.currentProjectRole.set(null);
      this.projectContext.setCurrentUserRole(null);
      this.canManage.set(true);
      return;
    }

    this.projectService.getCurrentMember(projectId).subscribe({
      next: membership => {
        const role = membership.status === 'ACTIVE' ? membership.projectRole : null;
        this.currentProjectRole.set(role);
        this.projectContext.setCurrentUserRole(role);
        this.canManage.set(role === 'PM');
      },
      error: () => {
        this.currentProjectRole.set(null);
        this.projectContext.setCurrentUserRole(null);
        this.canManage.set(false);
      },
    });
  }
}
