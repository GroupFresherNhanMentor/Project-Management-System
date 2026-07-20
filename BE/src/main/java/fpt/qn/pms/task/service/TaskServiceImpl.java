package fpt.qn.pms.task.service;

import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.TASK_ACTIVITIES;
import static fpt.qn.pms.jooq.Tables.USERS;

import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.enums.ActivityAction;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.TaskPriority;
import fpt.qn.pms.jooq.enums.TaskStatus;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;
import fpt.qn.pms.jooq.tables.records.TaskActivitiesRecord;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.security.annotation.RequireProjectRole;
import fpt.qn.pms.task.dto.AssignTaskRequest;
import fpt.qn.pms.task.dto.CreateTaskRequest;
import fpt.qn.pms.task.dto.TaskDto;
import fpt.qn.pms.task.dto.TaskSearchRequest;
import fpt.qn.pms.task.dto.UpdateTaskRequest;
import fpt.qn.pms.task.mapper.TaskMapper;
import fpt.qn.pms.task.repository.TaskRepository;
import fpt.qn.pms.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskServiceImpl implements TaskService {

    TaskRepository taskRepository;
    UserRepository userRepository;
    TaskMapper taskMapper;
    DSLContext dsl;

    private UsersRecord getCurrentUser() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Fallback: mock first user from database
            UsersRecord mockUser = dsl.selectFrom(USERS).limit(1).fetchOne();
            if (mockUser == null) {
                throw new AppException("No users found in database to mock authentication");
            }
            return mockUser;
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Current user not found"));
    }

    @Override
    @Transactional
    @RequireProjectRole(ProjectRole.PM)
    public TaskDto createTask(CreateTaskRequest request) {
        UsersRecord currentUser = getCurrentUser();

        // 1. Retrieve Project and generate Task Key
        ProjectsRecord project = dsl.selectFrom(PROJECTS)
                .where(PROJECTS.ID.eq(request.getProjectId()))
                .fetchOptional()
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        int nextNum = taskRepository.getNextTaskNumber(request.getProjectId());
        String taskKey = project.getProjectCode() + "-" + nextNum;

        // 3. Save TasksRecord
        TasksRecord record = taskMapper.toRecord(request);

        // Mock a reporter and check if they exist
        UUID reporterId = currentUser.getId();
        if (!userRepository.existsById(reporterId)) {
            reporterId = dsl.select(USERS.ID).from(USERS).limit(1).fetchOne(USERS.ID);
            if (reporterId == null) {
                throw new IllegalArgumentException("No users found in database to act as reporter");
            }
        }
        record.setReporterId(reporterId);

        record.setTaskKey(taskKey);
        record.setStatus(TaskStatus.TODO);
        record.setCreatedBy(currentUser.getId());
        record.setUpdatedBy(currentUser.getId());

        TasksRecord saved = taskRepository.create(record);

        // 4. Record activity
        TaskActivitiesRecord activity = dsl.newRecord(TASK_ACTIVITIES);
        activity.setTaskId(saved.getId());
        activity.setUserId(currentUser.getId());
        activity.setAction(ActivityAction.TASK_CREATED);
        activity.setNewValue("Task created with status TODO");
        activity.store();

        return toDtoWithUserNames(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto getTaskById(UUID id) {
        TasksRecord record = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
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
                .pageNumber(request.getPage())
                .pageSize(request.getSize())
                .build();
    }

    @Override
    @Transactional
    public TaskDto updateTask(UUID id, UpdateTaskRequest request) {
        UsersRecord currentUser = getCurrentUser();
        TasksRecord task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // 1. Check if PM
        boolean isPm = dsl.fetchExists(
                PROJECT_MEMBERS,
                PROJECT_MEMBERS.PROJECT_ID.eq(task.getProjectId())
                        .and(PROJECT_MEMBERS.USER_ID.eq(currentUser.getId()))
                        .and(PROJECT_MEMBERS.PROJECT_ROLE.eq(ProjectRole.PM))
        );

        // 2. Check if Dev (Assignee or Reporter)
        boolean isDev = currentUser.getId().equals(task.getAssigneeId()) || currentUser.getId().equals(task.getReporterId());

        if (!isPm && !isDev) {
            throw new AccessDeniedException("Access denied: You are not authorized to update this task");
        }

        // 3. Workflow transition check for Dev
        if (!isPm) {
            if (request.getStatus() != null && !request.getStatus().equals(task.getStatus())) {
                TaskStatus oldStatus = task.getStatus();
                TaskStatus newStatus = request.getStatus();
                boolean validTransition = false;
                if (oldStatus == TaskStatus.TODO && newStatus == TaskStatus.IN_PROGRESS) validTransition = true;
                else if (oldStatus == TaskStatus.IN_PROGRESS && newStatus == TaskStatus.TESTING) validTransition = true;
                else if (oldStatus == TaskStatus.TESTING && newStatus == TaskStatus.DONE) validTransition = true;
                else if (oldStatus == TaskStatus.IN_PROGRESS && newStatus == TaskStatus.TODO) validTransition = true;

                if (!validTransition) {
                    throw new IllegalArgumentException("Invalid status transition for developer: " + oldStatus + " -> " + newStatus);
                }
            }
        }

        // 4. Update task fields
        if (request.getStatus() != null && !request.getStatus().equals(task.getStatus())) {
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(request.getStatus());

            TaskActivitiesRecord activity = dsl.newRecord(TASK_ACTIVITIES);
            activity.setTaskId(task.getId());
            activity.setUserId(currentUser.getId());
            activity.setAction(ActivityAction.STATUS_CHANGED);
            activity.setOldValue(oldStatus.getLiteral());
            activity.setNewValue(request.getStatus().getLiteral());
            activity.store();
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

        task.setUpdatedBy(currentUser.getId());
        task.setUpdatedAt(java.time.OffsetDateTime.now());

        TasksRecord saved = taskRepository.update(task);
        return toDtoWithUserNames(saved);
    }

    @Override
    @Transactional
    @RequireProjectRole(ProjectRole.PM)
    public TaskDto assignTask(UUID id, AssignTaskRequest request) {
        UsersRecord currentUser = getCurrentUser();
        TasksRecord task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // 1. Check if assignee is member of the project
        boolean isMember = dsl.fetchExists(
                PROJECT_MEMBERS,
                PROJECT_MEMBERS.PROJECT_ID.eq(task.getProjectId())
                        .and(PROJECT_MEMBERS.USER_ID.eq(request.getAssigneeId()))
        );
        if (!isMember) {
            throw new IllegalArgumentException("Assignee must be a member of the project");
        }

        // 3. Assign and log activity
        UUID oldAssignee = task.getAssigneeId();
        task.setAssigneeId(request.getAssigneeId());
        task.setUpdatedBy(currentUser.getId());
        task.setUpdatedAt(java.time.OffsetDateTime.now());

        TasksRecord saved = taskRepository.update(task);

        TaskActivitiesRecord activity = dsl.newRecord(TASK_ACTIVITIES);
        activity.setTaskId(task.getId());
        activity.setUserId(currentUser.getId());
        activity.setAction(ActivityAction.ASSIGNEE_CHANGED);
        activity.setOldValue(oldAssignee != null ? oldAssignee.toString() : "Unassigned");
        activity.setNewValue(request.getAssigneeId().toString());
        activity.store();

        return toDtoWithUserNames(saved);
    }

    private TaskDto toDtoWithUserNames(TasksRecord record) {
        TaskDto dto = taskMapper.toDto(record);
        if (record.getAssigneeId() != null) {
            String name = dsl.select(USERS.FULL_NAME)
                    .from(USERS)
                    .where(USERS.ID.eq(record.getAssigneeId()))
                    .fetchOne(USERS.FULL_NAME);
            dto.setAssigneeName(name);
        }
        if (record.getReporterId() != null) {
            String name = dsl.select(USERS.FULL_NAME)
                    .from(USERS)
                    .where(USERS.ID.eq(record.getReporterId()))
                    .fetchOne(USERS.FULL_NAME);
            dto.setReporterName(name);
        }
        return dto;
    }
}
