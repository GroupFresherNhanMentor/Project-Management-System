package fpt.qn.pms.worklog.service.impl;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fpt.qn.pms.activity.event.TaskActivityEvent;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.exception.NotFoundException;
import fpt.qn.pms.jooq.enums.ActivityAction;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.jooq.tables.records.WorklogsRecord;
import fpt.qn.pms.security.ProjectSecurityEvaluator;
import fpt.qn.pms.security.UserPrincipal;
import fpt.qn.pms.task.repository.TaskRepository;
import fpt.qn.pms.user.repository.UserRepository;
import fpt.qn.pms.worklog.dto.CreateWorklogRequest;
import fpt.qn.pms.worklog.dto.UpdateWorklogRequest;
import fpt.qn.pms.worklog.dto.WorklogDto;
import fpt.qn.pms.worklog.dto.WorklogReportFilterDto;
import fpt.qn.pms.worklog.dto.WorklogReportItem;
import fpt.qn.pms.worklog.mapper.WorklogMapper;
import fpt.qn.pms.worklog.repository.WorklogRepository;
import fpt.qn.pms.worklog.service.WorklogService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorklogServiceImpl implements WorklogService {

    WorklogRepository worklogRepository;
    TaskRepository taskRepository;
    UserRepository userRepository;
    WorklogMapper worklogMapper;
    ProjectSecurityEvaluator projectSecurityEvaluator;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorklogDto> getWorklogsByTask(UUID taskId, int page, int size) {
        if (!taskRepository.existsById(taskId)) {
            throw new NotFoundException("Task not found");
        }

        PaginationResult<WorklogDto> result = worklogRepository.findByTaskId(taskId, page, size);
        return PageResponse.<WorklogDto>builder()
                .items(result.getItems())
                .totalElements(result.getTotal())
                .totalPages((int) Math.ceil((double) result.getTotal() / size))
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    @Override
    @Transactional
    public WorklogDto createWorklog(UUID taskId, CreateWorklogRequest request) {
        TasksRecord task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + taskId));

        if (!projectSecurityEvaluator.isAdmin() && !projectSecurityEvaluator.isMember(task.getProjectId())) {
            throw new AccessDeniedException("User is not an active member of this project");
        }

        UserPrincipal userPrincipal = projectSecurityEvaluator.getCurrentPrincipal()
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        WorklogsRecord record = worklogMapper.toRecord(request);
        record.setTaskId(taskId);
        record.setUserId(userPrincipal.getId());
        record.setCreatedAt(OffsetDateTime.now());

        WorklogsRecord saved = worklogRepository.create(record);
        WorklogDto dto = worklogRepository.findDtoById(saved.getId())
                .orElseThrow(() -> new NotFoundException("Worklog not found after creation"));

        eventPublisher.publishEvent(new TaskActivityEvent(
                taskId,
                userPrincipal.getId(),
                ActivityAction.WORKLOG_ADDED,
                null,
                String.valueOf(request.getHour())
        ));

        return dto;
    }

    @Override
    @Transactional
    public WorklogDto updateWorklog(UUID id, UpdateWorklogRequest request) {
        UserPrincipal userPrincipal = projectSecurityEvaluator.getCurrentPrincipal()
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        WorklogsRecord worklog = worklogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Worklog not found"));

        TasksRecord task = taskRepository.findById(worklog.getTaskId())
                .orElseThrow(() -> new NotFoundException("Task not found for worklog"));

        boolean isCreator = worklog.getUserId().equals(userPrincipal.getId());
        boolean isPm = projectSecurityEvaluator.isPm(task.getProjectId());
        boolean isAdmin = projectSecurityEvaluator.isAdmin();

        if (!isCreator && !isPm && !isAdmin) {
            throw new AccessDeniedException("Only the worklog creator, project PM, or an Admin can edit this worklog");
        }

        String oldHour = String.valueOf(worklog.getHours());

        worklog.setWorkDate(request.getWorkDate());
        worklog.setHours(request.getHour());
        worklog.setDescription(request.getDescription());

        WorklogsRecord saved = worklogRepository.update(worklog);
        WorklogDto dto = worklogRepository.findDtoById(saved.getId())
                .orElseThrow(() -> new NotFoundException("Worklog not found after update"));

        eventPublisher.publishEvent(new TaskActivityEvent(
                task.getId(),
                userPrincipal.getId(),
                ActivityAction.WORKLOG_UPDATED,
                oldHour,
                String.valueOf(request.getHour())
        ));

        return dto;
    }

    @Override
    @Transactional
    public void deleteWorklog(UUID id) {
        UserPrincipal userPrincipal = projectSecurityEvaluator.getCurrentPrincipal()
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        WorklogsRecord worklog = worklogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Worklog not found"));

        TasksRecord task = taskRepository.findById(worklog.getTaskId())
                .orElseThrow(() -> new NotFoundException("Task not found for worklog"));

        boolean isCreator = worklog.getUserId().equals(userPrincipal.getId());
        boolean isPm = projectSecurityEvaluator.isPm(task.getProjectId());
        boolean isAdmin = projectSecurityEvaluator.isAdmin();

        if (!isCreator && !isPm && !isAdmin) {
            throw new AccessDeniedException("Only the worklog creator, project PM, or an Admin can delete this worklog");
        }

        String hours = String.valueOf(worklog.getHours());
        UUID taskId = worklog.getTaskId();

        worklogRepository.hardDeleteById(id);

        eventPublisher.publishEvent(new TaskActivityEvent(
                taskId,
                userPrincipal.getId(),
                ActivityAction.WORKLOG_DELETED,
                hours,
                null
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorklogReportItem> getWorklogReport(WorklogReportFilterDto filter) {
        PaginationResult<WorklogReportItem> result = worklogRepository.getWorklogReport(filter);
        return PageResponse.<WorklogReportItem>builder()
                .items(result.getItems())
                .totalElements(result.getTotal())
                .totalPages((int) Math.ceil((double) result.getTotal() / filter.getSize()))
                .pageNumber(filter.getPage())
                .pageSize(filter.getSize())
                .build();
    }
}
