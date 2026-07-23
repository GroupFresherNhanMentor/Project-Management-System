package fpt.qn.pms.worklog.service;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.exception.NotFoundException;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
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

    @Mock
    ProjectSecurityEvaluator projectSecurityEvaluator;

    @InjectMocks
    WorklogServiceImpl worklogService;

    UUID worklogId;
    UUID taskId;
    UUID projectId;
    UUID userId;
    UUID otherUserId;
    UsersRecord currentUser;
    TasksRecord mockTaskRecord;
    WorklogsRecord mockWorklogRecord;
    WorklogDto mockWorklogDto;
    UserPrincipal currentUserPrincipal;
    UserPrincipal otherUserPrincipal;

    @BeforeEach
    void setUp() {
        worklogId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        currentUser = new UsersRecord();
        currentUser.setId(userId);
        currentUser.setUsername("dev1");
        currentUser.setFullName("Developer One");

        currentUserPrincipal = UserPrincipal.builder()
                .id(userId)
                .username("dev1")
                .build();

        otherUserPrincipal = UserPrincipal.builder()
                .id(otherUserId)
                .username("dev2")
                .build();

        mockTaskRecord = new TasksRecord();
        mockTaskRecord.setId(taskId);
        mockTaskRecord.setProjectId(projectId);

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

    private void mockCurrentUser(UserPrincipal principal) {
        when(projectSecurityEvaluator.getCurrentPrincipal()).thenReturn(Optional.of(principal));
    }

    // ── 1. Create Worklog ──────────────────────────────────────────────────────

    @Test
    @DisplayName("createWorklog - Should create worklog successfully")
    void createWorklog_success() {
        CreateWorklogRequest request = new CreateWorklogRequest();
        request.setWorkDate(LocalDate.now());
        request.setHour(BigDecimal.valueOf(4.5));
        request.setDescription("Implemented core logic");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTaskRecord));
        when(projectSecurityEvaluator.isMember(projectId)).thenReturn(true);
        mockCurrentUser(currentUserPrincipal);
        when(worklogMapper.toRecord(request)).thenReturn(new WorklogsRecord());
        when(worklogRepository.create(any(WorklogsRecord.class))).thenReturn(mockWorklogRecord);
        when(worklogRepository.findDtoById(worklogId)).thenReturn(Optional.of(mockWorklogDto));

        WorklogDto result = worklogService.createWorklog(taskId, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(worklogId);
        assertThat(result.getHour()).isEqualTo(BigDecimal.valueOf(4.5));
    }

    @Test
    @DisplayName("createWorklog - Should throw NotFoundException when Task not found")
    void createWorklog_taskNotFound_shouldThrowException() {
        CreateWorklogRequest request = new CreateWorklogRequest();
        request.setWorkDate(LocalDate.now());
        request.setHour(BigDecimal.valueOf(4.5));

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> worklogService.createWorklog(taskId, request))
                .isInstanceOf(NotFoundException.class);
    }

    // ── 2. Update Worklog ──────────────────────────────────────────────────────

    @Test
    @DisplayName("updateWorklog - Should update worklog when user is creator")
    void updateWorklog_success() {
        UpdateWorklogRequest request = new UpdateWorklogRequest();
        request.setWorkDate(LocalDate.now());
        request.setHour(BigDecimal.valueOf(6.0));
        request.setDescription("Updated description");

        mockCurrentUser(currentUserPrincipal);
        when(worklogRepository.findById(worklogId)).thenReturn(Optional.of(mockWorklogRecord));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTaskRecord));
        when(worklogRepository.update(mockWorklogRecord)).thenReturn(mockWorklogRecord);
        when(worklogRepository.findDtoById(worklogId)).thenReturn(Optional.of(mockWorklogDto));

        WorklogDto result = worklogService.updateWorklog(worklogId, request);

        assertThat(result).isNotNull();
        verify(worklogRepository).update(mockWorklogRecord);
    }

    @Test
    @DisplayName("updateWorklog - Should throw AccessDeniedException when user is not creator")
    void updateWorklog_notCreator_shouldThrowException() {
        UpdateWorklogRequest request = new UpdateWorklogRequest();

        mockCurrentUser(otherUserPrincipal);
        when(worklogRepository.findById(worklogId)).thenReturn(Optional.of(mockWorklogRecord));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTaskRecord));

        assertThatThrownBy(() -> worklogService.updateWorklog(worklogId, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only the worklog creator, project PM, or an Admin can edit this worklog");
    }

    // ── 3. Delete Worklog ──────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteWorklog - Should delete worklog when user is creator")
    void deleteWorklog_success() {
        mockCurrentUser(currentUserPrincipal);
        when(worklogRepository.findById(worklogId)).thenReturn(Optional.of(mockWorklogRecord));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTaskRecord));

        worklogService.deleteWorklog(worklogId);

        verify(worklogRepository).hardDeleteById(worklogId);
    }

    @Test
    @DisplayName("deleteWorklog - Should throw AccessDeniedException when user is not creator")
    void deleteWorklog_notCreator_shouldThrowException() {
        mockCurrentUser(otherUserPrincipal);
        when(worklogRepository.findById(worklogId)).thenReturn(Optional.of(mockWorklogRecord));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTaskRecord));

        assertThatThrownBy(() -> worklogService.deleteWorklog(worklogId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only the worklog creator, project PM, or an Admin can delete this worklog");
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
