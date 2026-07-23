package fpt.qn.pms.projectmember.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.ProjectMembersRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.project.exception.ProjectNotFoundException;
import fpt.qn.pms.project.repository.ProjectRepository;
import fpt.qn.pms.projectmember.dto.request.AddProjectMemberRequest;
import fpt.qn.pms.projectmember.dto.response.ProjectMemberCandidateDto;
import fpt.qn.pms.projectmember.dto.response.ProjectMemberDto;
import fpt.qn.pms.projectmember.exception.ProjectMemberAlreadyActiveException;
import fpt.qn.pms.projectmember.exception.ProjectMemberHasAssignedTasksException;
import fpt.qn.pms.projectmember.exception.ProjectMemberNotFoundException;
import fpt.qn.pms.projectmember.exception.ProjectMemberUserInactiveException;
import fpt.qn.pms.projectmember.mapper.ProjectMemberMapper;
import fpt.qn.pms.projectmember.repository.ProjectMemberRepository;
import fpt.qn.pms.projectmember.repository.projection.ProjectMemberDetails;
import fpt.qn.pms.projectmember.service.ProjectMemberAuthorizationService;
import fpt.qn.pms.projectmember.service.ProjectMemberService;
import fpt.qn.pms.task.repository.TaskRepository;
import fpt.qn.pms.user.exception.UserNotFoundException;
import fpt.qn.pms.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectMemberServiceImpl implements ProjectMemberService {

    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;
    TaskRepository taskRepository;
    UserRepository userRepository;
    ProjectMemberMapper projectMemberMapper;
    ProjectMemberAuthorizationService authorizationService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectMemberDto> getMembers(
            UUID projectId, String keyword, int page, int size) {
        requireProject(projectId);
        authorizationService.assertCanViewMembers(projectId);

        PaginationResult<ProjectMemberDetails> result = projectMemberRepository
                .findAllByProjectId(projectId, keyword, page, size);
        List<ProjectMemberDto> items = result.getItems().stream()
                .map(projectMemberMapper::toDto)
                .toList();
        return PageResponse.of(items, page, size, result.getTotal());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectMemberDto getCurrentMember(UUID projectId) {
        requireProject(projectId);
        UsersRecord currentUser = authorizationService.assertIsActiveProjectMember(projectId);
        return projectMemberRepository.findDetailsByProjectIdAndUserId(projectId, currentUser.getId())
                .map(projectMemberMapper::toDto)
                .orElseThrow(ProjectMemberNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectMemberCandidateDto> getMemberCandidates(
            UUID projectId, String keyword, int page, int size) {
        requireProject(projectId);
        authorizationService.assertCanManageMembers(projectId);

        var result = projectMemberRepository.findAvailableUsers(projectId, keyword, page, size);
        List<ProjectMemberCandidateDto> items = result.getItems().stream()
                .map(user -> ProjectMemberCandidateDto.builder()
                        .id(user.getId())
                        .employeeId(user.getEmployeeId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .build())
                .toList();
        return PageResponse.of(items, page, size, result.getTotal());
    }

    @Override
    @Transactional
    public ProjectMemberDto addMember(UUID projectId, AddProjectMemberRequest request) {
        requireProject(projectId);
        authorizationService.assertCanManageMembers(projectId);
        UsersRecord targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);
        if (targetUser.getStatus() != UserStatus.ACTIVE) {
            throw new ProjectMemberUserInactiveException();
        }

        ProjectMembersRecord record = projectMemberRepository
                .findByProjectIdAndUserId(projectId, request.getUserId())
                .map(existing -> reactivate(existing, request))
                .orElseGet(() -> create(projectId, request));

        return getDetails(projectId, record.getId());
    }

    @Override
    @Transactional
    public void removeMember(UUID projectId, UUID memberId) {
        lockProject(projectId);
        UsersRecord currentUser = authorizationService.assertCanManageMembers(projectId);

        ProjectMembersRecord record = projectMemberRepository.findByIdAndProjectId(memberId, projectId)
                .orElseThrow(ProjectMemberNotFoundException::new);
        UsersRecord targetUser = userRepository.findById(record.getUserId())
                .orElseThrow(UserNotFoundException::new);
        authorizationService.assertCanRemoveMember(currentUser, targetUser, record.getProjectRole());

        if (record.getStatus() == ProjectMemberStatus.INACTIVE) {
            return;
        }
        if (taskRepository.existsAssignedTaskByProjectIdAndAssigneeId(projectId, record.getUserId())) {
            throw new ProjectMemberHasAssignedTasksException();
        }

        record.setStatus(ProjectMemberStatus.INACTIVE);
        record.setUpdatedAt(OffsetDateTime.now());
        projectMemberRepository.update(record);
    }

    private ProjectMembersRecord reactivate(
            ProjectMembersRecord existing, AddProjectMemberRequest request) {
        if (existing.getStatus() == ProjectMemberStatus.ACTIVE) {
            throw new ProjectMemberAlreadyActiveException();
        }

        existing.setProjectRole(request.getProjectRole());
        existing.setStatus(ProjectMemberStatus.ACTIVE);
        existing.setUpdatedAt(OffsetDateTime.now());
        return projectMemberRepository.update(existing);
    }

    private ProjectMembersRecord create(UUID projectId, AddProjectMemberRequest request) {
        ProjectMembersRecord record = projectMemberMapper.toRecord(projectId, request);
        record.setStatus(ProjectMemberStatus.ACTIVE);
        return projectMemberRepository.create(record);
    }

    private ProjectMemberDto getDetails(UUID projectId, UUID memberId) {
        return projectMemberRepository.findDetailsByIdAndProjectId(memberId, projectId)
                .map(projectMemberMapper::toDto)
                .orElseThrow(ProjectMemberNotFoundException::new);
    }

    private void requireProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException();
        }
    }

    private void lockProject(UUID projectId) {
        if (!projectRepository.lockById(projectId)) {
            throw new ProjectNotFoundException();
        }
    }
}
