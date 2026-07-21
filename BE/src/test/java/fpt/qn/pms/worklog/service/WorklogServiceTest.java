package fpt.qn.pms.worklog.service;

import static fpt.qn.pms.jooq.Tables.USERS;
import static fpt.qn.pms.jooq.Tables.WORKLOGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
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
import fpt.qn.pms.worklog.service.impl.WorklogServiceImpl;

@ExtendWith(MockitoExtension.class)
class WorklogServiceTest {

    @Mock
    WorklogRepository worklogRepository;

    @Mock
    TaskRepository taskRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    WorklogMapper worklogMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    DSLContext dsl;

    @InjectMocks
    WorklogServiceImpl worklogService;

    UUID worklogId;
    UUID taskId;
    UUID userId;
    UUID otherUserId;
    UsersRecord currentUser;
    WorklogsRecord mockWorklogRecord;
    WorklogDto mockWorklogDto;

    @BeforeEach
    void setUp() {
        worklogId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        currentUser = new UsersRecord();
        currentUser.setId(userId);
        currentUser.setUsername("dev1");
        currentUser.setFullName("Developer One");

        mockWorklogRecord = new WorklogsRecord();
        mockWorklogRecord.setId(worklogId);
        mockWorklogRecord.setTaskId(taskId);
        mockWorklogRecord.setUserId(userId);
        mockWorklogRecord.setWorkDate(LocalDate.now());
        mockWorklogRecord.setHours(BigDecimal.valueOf(4.5));
        mockWorklogRecord.setDescription("Implemented core logic");

        mockWorklogDto = WorklogDto.builder()
                .id(worklogId)
                .taskId(taskId)
                .workDate(LocalDate.now())
                .hour(BigDecimal.valueOf(4.5))
                .description("Implemented core logic")
                .createdBy("Developer One")
                .build();
    }

    // ── 1. Create Worklog ──────────────────────────────────────────────────────

    @Test
    @DisplayName("createWorklog - Should create worklog successfully")
    void createWorklog_success() {
        CreateWorklogRequest request = new CreateWorklogRequest();
        request.setWorkDate(LocalDate.now());
        request.setHour(BigDecimal.valueOf(4.5));
        request.setDescription("Implemented core logic");

        when(taskRepository.existsById(taskId)).thenReturn(true);
        when(dsl.selectFrom(USERS).limit(1).fetchOne()).thenReturn(currentUser);
        when(worklogMapper.toRecord(request)).thenReturn(new WorklogsRecord());
        when(worklogRepository.create(any(WorklogsRecord.class))).thenReturn(mockWorklogRecord);
        when(worklogMapper.toDto(mockWorklogRecord)).thenReturn(mockWorklogDto);

        WorklogDto result = worklogService.createWorklog(taskId, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(worklogId);
        assertThat(result.getHour()).isEqualTo(BigDecimal.valueOf(4.5));
    }

    @Test
    @DisplayName("createWorklog - Should throw IllegalArgumentException when Task not found")
    void createWorklog_taskNotFound_shouldThrowException() {
        CreateWorklogRequest request = new CreateWorklogRequest();
        request.setWorkDate(LocalDate.now());
        request.setHour(BigDecimal.valueOf(4.5));

        when(taskRepository.existsById(taskId)).thenReturn(false);

        assertThatThrownBy(() -> worklogService.createWorklog(taskId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task not found");
    }

    // ── 2. Update Worklog ──────────────────────────────────────────────────────

    @Test
    @DisplayName("updateWorklog - Should update worklog when user is creator")
    void updateWorklog_success() {
        UpdateWorklogRequest request = new UpdateWorklogRequest();
        request.setWorkDate(LocalDate.now());
        request.setHour(BigDecimal.valueOf(6.0));
        request.setDescription("Updated description");

        when(worklogRepository.findById(worklogId)).thenReturn(Optional.of(mockWorklogRecord));
        when(dsl.selectFrom(USERS).limit(1).fetchOne()).thenReturn(currentUser);
        when(worklogRepository.update(mockWorklogRecord)).thenReturn(mockWorklogRecord);
        when(worklogMapper.toDto(mockWorklogRecord)).thenReturn(mockWorklogDto);

        WorklogDto result = worklogService.updateWorklog(worklogId, request);

        assertThat(result).isNotNull();
        verify(worklogRepository).update(mockWorklogRecord);
    }

    @Test
    @DisplayName("updateWorklog - Should throw AccessDeniedException when user is not creator")
    void updateWorklog_notCreator_shouldThrowException() {
        UpdateWorklogRequest request = new UpdateWorklogRequest();

        UsersRecord notCreator = new UsersRecord();
        notCreator.setId(otherUserId);

        when(worklogRepository.findById(worklogId)).thenReturn(Optional.of(mockWorklogRecord));
        when(dsl.selectFrom(USERS).limit(1).fetchOne()).thenReturn(notCreator);

        assertThatThrownBy(() -> worklogService.updateWorklog(worklogId, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only the creator of the worklog can edit it");
    }

    // ── 3. Delete Worklog ──────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteWorklog - Should delete worklog when user is creator")
    void deleteWorklog_success() {
        when(worklogRepository.findById(worklogId)).thenReturn(Optional.of(mockWorklogRecord));
        when(dsl.selectFrom(USERS).limit(1).fetchOne()).thenReturn(currentUser);

        worklogService.deleteWorklog(worklogId);

        verify(worklogRepository).deleteById(worklogId);
    }

    @Test
    @DisplayName("deleteWorklog - Should throw AccessDeniedException when user is not creator")
    void deleteWorklog_notCreator_shouldThrowException() {
        UsersRecord notCreator = new UsersRecord();
        notCreator.setId(otherUserId);

        when(worklogRepository.findById(worklogId)).thenReturn(Optional.of(mockWorklogRecord));
        when(dsl.selectFrom(USERS).limit(1).fetchOne()).thenReturn(notCreator);

        assertThatThrownBy(() -> worklogService.deleteWorklog(worklogId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only the creator of the worklog can delete it");
    }

    // ── 4. Worklog Report ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getWorklogReport - Should return paginated report")
    void getWorklogReport_success() {
        WorklogReportFilterDto filter = new WorklogReportFilterDto();
        filter.setPage(0);
        filter.setSize(10);

        WorklogReportItem item = WorklogReportItem.builder()
                .id(worklogId)
                .taskId(taskId)
                .taskKey("WEB-1")
                .taskSummary("Setup Task Test")
                .userId(userId)
                .userName("Developer One")
                .workDate(LocalDate.now())
                .hour(BigDecimal.valueOf(4.5))
                .description("Implemented core logic")
                .build();

        PaginationResult<WorklogReportItem> paginationResult =
                new PaginationResult<>(1L, List.of(item));

        when(worklogRepository.getWorklogReport(filter)).thenReturn(paginationResult);

        PageResponse<WorklogReportItem> response = worklogService.getWorklogReport(filter);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1L);
    }
}
