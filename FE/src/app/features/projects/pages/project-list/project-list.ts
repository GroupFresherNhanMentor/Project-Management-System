import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectService } from '../../../../core/services/project';
import { ProjectDto, ProjectListParams } from '../../../../core/models/project.model';
import { PageResponse } from '../../../../core/models/api.model';

@Component({
  selector: 'app-project-list',
  imports: [CommonModule],
  templateUrl: './project-list.html',
})
export class ProjectList implements OnInit {
  private readonly projectService = inject(ProjectService);

  readonly data = signal<PageResponse<ProjectDto> | null>(null);
  readonly params = signal<ProjectListParams>({ page: 0, size: 20 });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.projectService.getProjects(this.params()).subscribe({
      next: res => this.data.set(res),
    });
  }
}
