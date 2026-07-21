import { Component, signal, inject, effect } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectMemberDto } from '../../../../core/models/project-member.model';
import { AuthService } from '../../../../core/services/auth';
import { ProjectService } from '../../../../core/services/project';
import { ProjectContextService } from '../../../../core/services/project-context';
import { ToastService } from '../../../../core/services/toast';
import { InitialsPipe } from '../../../../shared/pipes/initials.pipe';

@Component({
  selector: 'app-member-list',
  imports: [InitialsPipe],
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

  constructor() {
    effect(() => {
      const id = this.route.snapshot.queryParamMap.get('projectId')
        ?? this.projectContext.selectedProjectId();
      if (id && id !== this.loadedProjectId) {
        this.loadedProjectId = id;
        this.projectId.set(id);
        this.load(id);
      }
    });
  }

  load(projectId = this.projectId()): void {
    if (!projectId) return;
    this.loading.set(true);
    this.errorMessage.set(null);
    this.projectService.getMembers(projectId, 0, 100).subscribe({
      next: page => {
        this.members.set(page.items);
        const currentUserId = this.authService.getCurrentUser()?.id;
        const currentMembership = page.items.find(
          item => item.userId === currentUserId && item.status === 'ACTIVE');
        this.projectContext.setCurrentUserRole(currentMembership?.projectRole ?? null);
        this.canManage.set(
          this.authService.getCurrentUser()?.role === 'ADMIN'
          || currentMembership?.projectRole === 'PM');
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(error.error?.message ?? 'Unable to load project members.');
        this.loading.set(false);
      },
    });
  }

  newMember(): void {
    void this.router.navigate(['/members/new'], { queryParams: { projectId: this.projectId() } });
  }

  removeMember(member: ProjectMemberDto): void {
    const projectId = this.projectId();
    if (!projectId || member.status !== 'ACTIVE') return;
    this.projectService.removeMember(projectId, member.id).subscribe({
      next: () => {
        this.toast.success('Member removed.');
        this.load(projectId);
      },
      error: (error: HttpErrorResponse) =>
        this.toast.error(error.error?.message ?? 'Unable to remove member.'),
    });
  }

  statusBadge(s: string): string {
    return s === 'ACTIVE' ? 'badge badge-active' : 'badge badge-locked';
  }
}
