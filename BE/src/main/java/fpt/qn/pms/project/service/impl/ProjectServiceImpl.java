package fpt.qn.pms.project.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;
import fpt.qn.pms.project.dto.request.CreateProjectRequest;
import fpt.qn.pms.project.dto.request.UpdateProjectRequest;
import fpt.qn.pms.project.dto.response.ProjectDto;
import fpt.qn.pms.project.exception.InvalidInitialProjectStatusException;
import fpt.qn.pms.project.exception.InvalidProjectDateRangeException;
import fpt.qn.pms.project.exception.ProjectAccessDeniedException;
import fpt.qn.pms.project.exception.ProjectCodeAlreadyExistsException;
import fpt.qn.pms.project.exception.ProjectNotFoundException;
import fpt.qn.pms.project.mapper.ProjectMapper;
import fpt.qn.pms.project.repository.ProjectRepository;
import fpt.qn.pms.project.service.ProjectService;
import fpt.qn.pms.projectmember.repository.ProjectMemberRepository;
import fpt.qn.pms.security.ProjectSecurityEvaluator;
import fpt.qn.pms.security.UserPrincipal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;
    ProjectMapper projectMapper;
    ProjectSecurityEvaluator securityEvaluator;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectDto> getProjects(String keyword, ProjectStatus status, int page, int size) {
        UserPrincipal principal = getCurrentPrincipal();
        UUID memberUserId = isAdmin(principal) ? null : principal.getId();

        PaginationResult<ProjectsRecord> result = projectRepository
                .findAll(keyword, status, memberUserId, page, size);
        List<ProjectDto> items = projectMapper.toDtoList(result.getItems());
        return PageResponse.of(items, page, size, result.getTotal());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDto getProjectById(UUID projectId) {
        UserPrincipal principal = getCurrentPrincipal();
        ProjectsRecord project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException());

        if (!isAdmin(principal)
                && !projectMemberRepository.existsActiveByProjectIdAndUserId(projectId, principal.getId())) {
            throw new ProjectAccessDeniedException();
        }

        return projectMapper.toDto(project);
    }

    @Override
    @Transactional
    public ProjectDto createProject(CreateProjectRequest request) {
        UserPrincipal principal = getCurrentPrincipal();
        validateDateRange(request.getStartDate(), request.getEndDate());
        if (request.getStatus() == ProjectStatus.COMPLETED) {
            throw new InvalidInitialProjectStatusException();
        }

        String normalizedCode = request.getProjectCode().trim().toUpperCase(Locale.ROOT);
        if (projectRepository.existsByProjectCodeIgnoreCase(normalizedCode)) {
            throw new ProjectCodeAlreadyExistsException();
        }

        ProjectsRecord record = projectMapper.toRecord(request);
        record.setProjectCode(normalizedCode);
        record.setProjectName(request.getProjectName().trim());
        record.setCreatedBy(principal.getId());
        record.setUpdatedBy(principal.getId());

        return projectMapper.toDto(projectRepository.create(record));
    }

    @Override
    @Transactional
    public ProjectDto updateProject(UUID projectId, UpdateProjectRequest request) {
        UserPrincipal principal = getCurrentPrincipal();
        ProjectsRecord record = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException());

        validateDateRange(request.getStartDate(), request.getEndDate());
        projectMapper.updateRecord(record, request);
        record.setProjectName(request.getProjectName().trim());
        record.setUpdatedBy(principal.getId());

        return projectMapper.toDto(projectRepository.update(record));
    }

    private void validateDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidProjectDateRangeException();
        }
    }

    private UserPrincipal getCurrentPrincipal() {
        return securityEvaluator.getCurrentPrincipal()
                .orElseThrow(() -> new ProjectAccessDeniedException());
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> SysRole.ADMIN.getLiteral().equals(a.getAuthority()));
    }
}
