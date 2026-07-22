package fpt.qn.pms.project.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.project.dto.request.CreateProjectRequest;
import fpt.qn.pms.project.dto.request.UpdateProjectRequest;
import fpt.qn.pms.project.dto.response.ProjectDto;
import fpt.qn.pms.project.exception.InvalidProjectDateRangeException;
import fpt.qn.pms.project.exception.ProjectAccessDeniedException;
import fpt.qn.pms.project.exception.ProjectCodeAlreadyExistsException;
import fpt.qn.pms.project.exception.ProjectNotFoundException;
import fpt.qn.pms.project.mapper.ProjectMapper;
import fpt.qn.pms.project.repository.ProjectRepository;
import fpt.qn.pms.project.service.ProjectService;
import fpt.qn.pms.projectmember.repository.ProjectMemberRepository;
import fpt.qn.pms.user.exception.UserNotFoundException;
import fpt.qn.pms.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectDto> getProjects(String keyword, ProjectStatus status, int page, int size) {
        UsersRecord currentUser = getCurrentUser();
        UUID memberUserId = currentUser.getRole() == SysRole.ADMIN ? null : currentUser.getId();

        if (memberUserId != null
                && !projectMemberRepository.existsActiveByUserIdAndRole(memberUserId, ProjectRole.PM)) {
            throw new ProjectAccessDeniedException();
        }

        PaginationResult<ProjectsRecord> result = projectRepository
                .findAll(keyword, status, memberUserId, page, size);
        List<ProjectDto> items = projectMapper.toDtoList(result.getItems());
        return PageResponse.of(items, page, size, result.getTotal());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDto getProjectById(UUID projectId) {
        UsersRecord currentUser = getCurrentUser();
        ProjectsRecord project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException());

        if (currentUser.getRole() != SysRole.ADMIN
                && !projectMemberRepository.existsActiveByProjectIdAndUserIdAndRole(
                        projectId, currentUser.getId(), ProjectRole.PM)) {
            throw new ProjectAccessDeniedException();
        }

        return projectMapper.toDto(project);
    }

    @Override
    @Transactional
    public ProjectDto createProject(CreateProjectRequest request) {
        UsersRecord currentUser = getCurrentUser();
        validateDateRange(request.getStartDate(), request.getEndDate());

        String normalizedCode = request.getProjectCode().trim().toUpperCase(Locale.ROOT);
        if (projectRepository.existsByProjectCodeIgnoreCase(normalizedCode)) {
            throw new ProjectCodeAlreadyExistsException();
        }

        ProjectsRecord record = projectMapper.toRecord(request);
        record.setProjectCode(normalizedCode);
        record.setProjectName(request.getProjectName().trim());
        record.setCreatedBy(currentUser.getId());
        record.setUpdatedBy(currentUser.getId());

        return projectMapper.toDto(projectRepository.create(record));
    }

    @Override
    @Transactional
    public ProjectDto updateProject(UUID projectId, UpdateProjectRequest request) {
        UsersRecord currentUser = getCurrentUser();
        ProjectsRecord record = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException());

        validateDateRange(request.getStartDate(), request.getEndDate());
        projectMapper.updateRecord(record, request);
        record.setProjectName(request.getProjectName().trim());
        record.setUpdatedBy(currentUser.getId());

        return projectMapper.toDto(projectRepository.update(record));
    }

    private void validateDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidProjectDateRangeException();
        }
    }

    private UsersRecord getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ProjectAccessDeniedException();
        }

        UsersRecord currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException());
        if (currentUser.getStatus() != UserStatus.ACTIVE) {
            throw new ProjectAccessDeniedException();
        }
        return currentUser;
    }
}
