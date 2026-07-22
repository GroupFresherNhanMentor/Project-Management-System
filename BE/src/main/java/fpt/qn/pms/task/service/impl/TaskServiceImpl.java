package fpt.qn.pms.task.service.impl;

import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.user.exception.UserNotFoundException;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.TaskStatus;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.project.exception.ProjectNotFoundException;
import fpt.qn.pms.project.repository.ProjectRepository;
import fpt.qn.pms.projectmember.repository.ProjectMemberRepository;
import fpt.qn.pms.security.ProjectSecurityEvaluator;
import fpt.qn.pms.security.annotation.RequireProjectRole;
import fpt.qn.pms.task.dto.AssignTaskRequest;
import fpt.qn.pms.task.dto.CreateTaskRequest;
import fpt.qn.pms.task.dto.TaskDto;
import fpt.qn.pms.task.dto.TaskSearchRequest;
import fpt.qn.pms.task.dto.UpdateTaskRequest;
import fpt.qn.pms.task.exception.AssigneeNotInProjectException;
import fpt.qn.pms.task.exception.InvalidTaskStatusTransitionException;
import fpt.qn.pms.task.exception.TaskNotFoundException;
import fpt.qn.pms.task.mapper.TaskMapper;
import fpt.qn.pms.task.repository.TaskRepository;
import fpt.qn.pms.task.service.TaskService;
import fpt.qn.pms.user.repository.UserRepository;
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
    UserRepository userRepository;
    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;
    ProjectSecurityEvaluator projectSecurityEvaluator;
    TaskMapper taskMapper;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @RequireProjectRole(ProjectRole.PM)
    public TaskDto createTask(CreateTaskRequest request) {
        UUID currentUserId = projectSecurityEvaluator.getCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException());

        // 1. Retrieve Project and generate Task Key
        ProjectsRecord project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException());

        int nextNum = taskRepository.getNextTaskNumber(request.getProjectId());
        String taskKey = project.getProjectCode() + "-" + nextNum;

        // 3. Save TasksRecord
        TasksRecord record = taskMapper.toRecord(request);

        record.setReporterId(currentUserId);

        record.setTaskKey(taskKey);
        record.setStatus(TaskStatus.TODO);
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

        return toDtoWithUserNames(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto getTaskById(UUID id) {
        TasksRecord record =
                taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException());
        return toDtoWithUserNames(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskDto> searchTasks(TaskSearchRequest request) {
        PaginationResult<TasksRecord> result = taskRepository.findAll(request);
        return PageResponse.<TaskDto>builder()
                .items(result.getItems().stream().map(this::toDtoWithUserNames).toList())
                .totalElements(result.getTotal())
                .totalPages((int) Math.ceil((double) result.getTotal() / request.getSize()))
                .pageNumber(request.getPage()).pageSize(request.getSize()).build();
    }

    @Override
    @Transactional
    public TaskDto updateTask(UUID id, UpdateTaskRequest request) {

        UUID currentUserId = projectSecurityEvaluator.getCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException());

        TasksRecord task =
                taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException());

        // 1. Check if PM
        boolean isPm = projectSecurityEvaluator.isPm(id);

        // 2. Check if Dev (Assignee or Reporter)
        boolean isDev = currentUserId.equals(task.getAssigneeId())
                || currentUserId.equals(task.getReporterId());

        if (!isPm && !isDev) {
            throw new AccessDeniedException(
                    "Access denied: You are not authorized to update this task");
        }

        // 3. Workflow transition check for Dev
        if (!isPm) {
            if (request.getStatus() != null && !request.getStatus().equals(task.getStatus())) {
                TaskStatus oldStatus = task.getStatus();
                TaskStatus newStatus = request.getStatus();
                boolean validTransition = false;
                // if (oldStatus == TaskStatus.TODO && newStatus == TaskStatus.IN_PROGRESS)
                // validTransition = true;
                // else if (oldStatus == TaskStatus.IN_PROGRESS && newStatus == TaskStatus.TESTING)
                // validTransition = true;
                // else if (oldStatus == TaskStatus.TESTING && newStatus == TaskStatus.DONE)
                // validTransition = true;
                // else if (oldStatus == TaskStatus.IN_PROGRESS && newStatus == TaskStatus.TODO)
                // validTransition = true;

                if (!validTransition) {
                    throw new InvalidTaskStatusTransitionException(
                            "Invalid status transition for developer: " + oldStatus + " -> "
                                    + newStatus);
                }
            }
        }

        // 4. Update task fields
        if (request.getStatus() != null && !request.getStatus().equals(task.getStatus())) {
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(request.getStatus());

            eventPublisher.publishEvent(new TaskActivityEvent(
                    task.getId(),
                    currentUserId,
                    ActivityAction.STATUS_CHANGED,
                    oldStatus != null ? oldStatus.getLiteral() : null,
                    request.getStatus().getLiteral()
            ));
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
        task.setUpdatedAt(java.time.OffsetDateTime.now());

        TasksRecord saved = taskRepository.update(task);
        return toDtoWithUserNames(saved);
    }

    @Override
    @Transactional
    @RequireProjectRole(ProjectRole.PM)
    public TaskDto assignTask(UUID id, AssignTaskRequest request) {
        UUID currentUserId = projectSecurityEvaluator.getCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException());
        TasksRecord task =
                taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException());

        // 1. Check if assignee is member of the project
        // boolean isMember = dsl.fetchExists(PROJECT_MEMBERS, PROJECT_MEMBERS.PROJECT_ID
        //         .eq(task.getProjectId()).and(PROJECT_MEMBERS.USER_ID.eq(request.getAssigneeId())));
        boolean isMember = projectMemberRepository.findByProjectIdAndUserId(task.getProjectId(), request.getAssigneeId())
                .map(member -> member.getStatus() == ProjectMemberStatus.ACTIVE)
                .orElse(false);
        if (!isMember) {
            throw new AssigneeNotInProjectException();
        }

        // 3. Assign and log activity
        UUID oldAssignee = task.getAssigneeId();
        task.setAssigneeId(request.getAssigneeId());
        task.setUpdatedBy(currentUserId);
        task.setUpdatedAt(java.time.OffsetDateTime.now());

        TasksRecord saved = taskRepository.update(task);

        eventPublisher.publishEvent(new TaskActivityEvent(
                task.getId(),
                currentUserId,
                ActivityAction.ASSIGNEE_CHANGED,
                oldAssignee != null ? oldAssignee.toString() : "Unassigned",
                request.getAssigneeId().toString()
        ));

        return toDtoWithUserNames(saved);
    }

    private TaskDto toDtoWithUserNames(TasksRecord record) {
        TaskDto dto = taskMapper.toDto(record);
        if (record.getAssigneeId() != null) {
            String name = userRepository.findById(record.getAssigneeId())
                    .map(UsersRecord::getFullName)
                    .orElse(null);
            dto.setAssigneeName(name);
        }
        if (record.getReporterId() != null) {
            String name = userRepository.findById(record.getReporterId())
                    .map(UsersRecord::getFullName)
                    .orElse(null);
            dto.setReporterName(name);
        }
        return dto;
    }
}
