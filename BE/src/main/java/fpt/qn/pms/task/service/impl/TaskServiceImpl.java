package fpt.qn.pms.task.service.impl;

import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.user.exception.UserNotFoundException;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.project.exception.ProjectNotFoundException;
import fpt.qn.pms.project.repository.ProjectRepository;
import fpt.qn.pms.projectmember.repository.ProjectMemberRepository;
import fpt.qn.pms.security.ProjectSecurityEvaluator;
import fpt.qn.pms.task.dto.AssignTaskRequest;
import fpt.qn.pms.task.dto.CreateTaskRequest;
import fpt.qn.pms.task.dto.TaskDto;
import fpt.qn.pms.task.dto.TaskSearchRequest;
import fpt.qn.pms.task.dto.UpdateTaskRequest;
import fpt.qn.pms.task.exception.AssigneeNotInProjectException;
import fpt.qn.pms.task.exception.InvalidTaskStatusTransitionException;
import fpt.qn.pms.task.exception.ReporterNotInProjectException;
import fpt.qn.pms.task.exception.TaskNotFoundException;
import fpt.qn.pms.task.exception.TaskStatusNotFoundException;
import fpt.qn.pms.task.mapper.TaskMapper;
import fpt.qn.pms.task.repository.TaskRepository;
import fpt.qn.pms.task.repository.TaskStatusRepository;
import fpt.qn.pms.task.repository.TaskWorkflowRepository;
import fpt.qn.pms.task.repository.resultModel.TaskResult;
import fpt.qn.pms.task.service.TaskService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.context.ApplicationEventPublisher;
import fpt.qn.pms.activity.event.TaskActivityEvent;
import fpt.qn.pms.jooq.enums.ActivityAction;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskServiceImpl implements TaskService {

    TaskRepository taskRepository;
    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;
    ProjectSecurityEvaluator projectSecurityEvaluator;
    TaskStatusRepository taskStatusRepository;
    TaskWorkflowRepository taskWorkflowRepository;
    TaskMapper taskMapper;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public TaskDto createTask(CreateTaskRequest request) {
        UUID currentUserId = projectSecurityEvaluator.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElseThrow(() -> new UserNotFoundException());

        projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException());

        if (request.getAssigneeId() != null) {
            boolean isAssigneeMember = projectMemberRepository
                    .findByProjectIdAndUserId(request.getProjectId(), request.getAssigneeId())
                    .map(member -> member.getStatus() == ProjectMemberStatus.ACTIVE)
                    .orElse(false);

         
            if (!isAssigneeMember) {
                throw new AssigneeNotInProjectException();
            }

        }

        if (request.getReporterId() != null) {
            boolean isReporterMember = projectMemberRepository
                    .findByProjectIdAndUserId(request.getProjectId(), request.getReporterId())
                    .map(member -> member.getStatus() == ProjectMemberStatus.ACTIVE)
                    .orElse(false);

            if (!isReporterMember) {
                throw new ReporterNotInProjectException();
            }
        }

        UUID resolvedStatusId = taskStatusRepository.findById(request.getTaskStatusId())
                .filter(s -> s.getProjectId().equals(request.getProjectId()))
                .orElseThrow(() -> new TaskStatusNotFoundException(request.getTaskStatusId()))
                .getId();

        TasksRecord record = taskMapper.toRecord(request);
        record.setReporterId(currentUserId);
        record.setStatusId(resolvedStatusId);
        record.setCreatedBy(currentUserId);
        record.setUpdatedBy(currentUserId);

        TasksRecord saved = taskRepository.create(record);

        // Record activity via Spring Event
        eventPublisher.publishEvent(new TaskActivityEvent(
                saved.getId(),
                currentUserId,
                ActivityAction.TASK_CREATED,
                null,
                saved.getSummary()
        ));

        return taskRepository.findDetailById(saved.getId()).map(taskMapper::toDto).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto getTaskById(UUID id) {
        return taskRepository.findDetailById(id).map(taskMapper::toDto).orElseThrow(() -> new TaskNotFoundException());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskDto> searchTasks(TaskSearchRequest request) {
        PaginationResult<TaskResult> result = taskRepository.findAll(request);
        return PageResponse.<TaskDto>builder()
                .items(result.getItems().stream().map(taskMapper::toDto).toList())
                .totalElements(result.getTotal())
                .totalPages((int) Math.ceil((double) result.getTotal() / request.getSize()))
                .pageNumber(request.getPage()).pageSize(request.getSize()).build();
    }

    @Override
    @Transactional
    public TaskDto updateTask(UUID id, UpdateTaskRequest request) {

        UUID currentUserId = projectSecurityEvaluator.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElseThrow(() -> new UserNotFoundException());

        TasksRecord task =
                taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException());

        boolean isPm = projectSecurityEvaluator.isPm(task.getProjectId());
        boolean isDev = currentUserId.equals(task.getAssigneeId())
                || currentUserId.equals(task.getReporterId());

        if (!isPm && !isDev) {
            throw new AccessDeniedException(
                    "Access denied: You are not authorized to update this task");
        }

        String oldStatusName = null;
        String newStatusName = null;

        if (request.getStatusId() != null && !request.getStatusId().equals(task.getStatusId())) {
            UUID newStatusId = request.getStatusId();

            var newStatus = taskStatusRepository.findById(newStatusId)
                    .filter(s -> s.getProjectId().equals(task.getProjectId()))
                    .orElseThrow(() -> new TaskStatusNotFoundException(newStatusId));

            if (!isPm) {
                UUID currentStatusId = task.getStatusId();
                boolean validTransition = currentStatusId != null
                        && taskWorkflowRepository.existsByFromStatusIdAndToStatusId(
                                currentStatusId, newStatusId);
                if (!validTransition) {
                    throw new InvalidTaskStatusTransitionException(
                            "Transition not allowed by project workflow");
                }
            }

            oldStatusName = task.getStatusId() != null
                    ? taskStatusRepository.findById(task.getStatusId())
                            .map(s -> s.getName())
                            .orElse(task.getStatusId().toString())
                    : null;
            newStatusName = newStatus.getName();

            task.setStatusId(newStatusId);
        }

        String oldPriority = null;
        String newPriority = null;

        if (request.getPriority() != null && !request.getPriority().equals(task.getPriority())) {
            oldPriority = task.getPriority() != null ? task.getPriority().getLiteral() : null;
            newPriority = request.getPriority().getLiteral();
            task.setPriority(request.getPriority());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getEstimateHour() != null) {
            task.setEstimateHour(request.getEstimateHour());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        task.setUpdatedBy(currentUserId);

        taskRepository.update(task);

        if (newStatusName != null) {
            eventPublisher.publishEvent(new TaskActivityEvent(
                    id,
                    currentUserId,
                    ActivityAction.STATUS_CHANGED,
                    oldStatusName,
                    newStatusName
            ));
        }

        if (newPriority != null) {
            eventPublisher.publishEvent(new TaskActivityEvent(
                    id,
                    currentUserId,
                    ActivityAction.PRIORITY_CHANGED,
                    oldPriority,
                    newPriority
            ));
        }

        return taskRepository.findDetailById(id).map(taskMapper::toDto).orElseThrow();
    }

    @Override
    @Transactional
    public TaskDto assignTask(UUID id, AssignTaskRequest request) {
        UUID currentUserId = projectSecurityEvaluator.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElseThrow(() -> new UserNotFoundException());
        TasksRecord task =
                taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException());

        if (request.getAssigneeId() != null) {
            boolean isMember = projectMemberRepository
                    .findByProjectIdAndUserId(task.getProjectId(), request.getAssigneeId())
                    .map(member -> member.getStatus() == ProjectMemberStatus.ACTIVE)
                    .orElse(false);
            if (!isMember) {
                throw new AssigneeNotInProjectException();
            }
        }

        UUID oldAssigneeId = task.getAssigneeId();
        task.setAssigneeId(request.getAssigneeId());
        task.setUpdatedBy(currentUserId);

        taskRepository.update(task);

        eventPublisher.publishEvent(new TaskActivityEvent(
                task.getId(),
                currentUserId,
                ActivityAction.ASSIGNEE_CHANGED,
                oldAssigneeId != null ? oldAssigneeId.toString() : null,
                request.getAssigneeId() != null ? request.getAssigneeId().toString() : null
        ));

        return taskRepository.findDetailById(id).map(taskMapper::toDto).orElseThrow();
    }
}
