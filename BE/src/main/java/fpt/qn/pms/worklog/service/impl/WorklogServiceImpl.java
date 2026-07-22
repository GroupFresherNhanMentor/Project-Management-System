package fpt.qn.pms.worklog.service.impl;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.security.ProjectSecurityEvaluator;
import fpt.qn.pms.jooq.tables.records.WorklogsRecord;
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

    private UsersRecord getCurrentUser() {
        return projectSecurityEvaluator.getCurrentPrincipal()
                .map(p -> p.getUsername())
                .flatMap(userRepository::findByUsername)
                .orElseGet(() -> userRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                                "No users found in database to mock authentication")));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorklogDto> getWorklogsByTask(UUID taskId, int page, int size) {
        if (!taskRepository.existsById(taskId)) {
            throw new IllegalArgumentException("Task not found");
        }

        PaginationResult<WorklogsRecord> result =
                worklogRepository.findByTaskId(taskId, page, size);
        return PageResponse.<WorklogDto>builder()
                .items(result.getItems().stream().map(this::toDtoWithUserName).toList())
                .totalElements(result.getTotal())
                .totalPages((int) Math.ceil((double) result.getTotal() / size))
                .pageNumber(page).pageSize(size).build();
    }

    @Override
    @Transactional
    public WorklogDto createWorklog(UUID taskId, CreateWorklogRequest request) {
        if (!taskRepository.existsById(taskId)) {
            throw new IllegalArgumentException("Task not found");
        }

        UsersRecord currentUser = getCurrentUser();

        WorklogsRecord record = worklogMapper.toRecord(request);
        record.setTaskId(taskId);
        record.setUserId(currentUser.getId());
        record.setCreatedAt(OffsetDateTime.now());

        WorklogsRecord saved = worklogRepository.create(record);
        return toDtoWithUserName(saved);
    }

    @Override
    @Transactional
    public WorklogDto updateWorklog(UUID id, UpdateWorklogRequest request) {
        WorklogsRecord worklog = worklogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Worklog not found"));

        UsersRecord currentUser = getCurrentUser();
        if (!worklog.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the creator of the worklog can edit it");
        }

        worklog.setWorkDate(request.getWorkDate());
        worklog.setHours(request.getHour());
        worklog.setDescription(request.getDescription());

        WorklogsRecord saved = worklogRepository.update(worklog);
        return toDtoWithUserName(saved);
    }

    @Override
    @Transactional
    public void deleteWorklog(UUID id) {
        WorklogsRecord worklog = worklogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Worklog not found"));

        UsersRecord currentUser = getCurrentUser();
        if (!worklog.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the creator of the worklog can delete it");
        }

        worklogRepository.hardDeleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorklogReportItem> getWorklogReport(WorklogReportFilterDto filter) {
        PaginationResult<WorklogReportItem> result = worklogRepository.getWorklogReport(filter);
        return PageResponse.<WorklogReportItem>builder().items(result.getItems())
                .totalElements(result.getTotal())
                .totalPages((int) Math.ceil((double) result.getTotal() / filter.getSize()))
                .pageNumber(filter.getPage()).pageSize(filter.getSize()).build();
    }

    private WorklogDto toDtoWithUserName(WorklogsRecord record) {
        WorklogDto dto = worklogMapper.toDto(record);
        if (record.getUserId() != null) {
            String userName = userRepository.findById(record.getUserId())
                    .map(UsersRecord::getFullName)
                    .orElse(null);
            dto.setCreatedBy(userName);
        }
        return dto;
    }
}
