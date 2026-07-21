package fpt.qn.pms.project.service;

import java.util.UUID;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.project.dto.request.CreateProjectRequest;
import fpt.qn.pms.project.dto.request.UpdateProjectRequest;
import fpt.qn.pms.project.dto.response.ProjectDto;

public interface ProjectService {

    PageResponse<ProjectDto> getProjects(String keyword, ProjectStatus status, int page, int size);

    ProjectDto getProjectById(UUID projectId);

    ProjectDto createProject(CreateProjectRequest request);

    ProjectDto updateProject(UUID projectId, UpdateProjectRequest request);
}
