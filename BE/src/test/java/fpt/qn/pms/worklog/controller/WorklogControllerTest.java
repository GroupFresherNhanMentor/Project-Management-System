package fpt.qn.pms.worklog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.worklog.dto.CreateWorklogRequest;
import fpt.qn.pms.worklog.dto.UpdateWorklogRequest;
import fpt.qn.pms.worklog.dto.WorklogDto;
import fpt.qn.pms.worklog.dto.WorklogReportFilterDto;
import fpt.qn.pms.worklog.dto.WorklogReportItem;
import fpt.qn.pms.worklog.service.WorklogService;

@ExtendWith(MockitoExtension.class)
class WorklogControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @Mock
    WorklogService worklogService;

    @InjectMocks
    WorklogController worklogController;

    UUID taskId;
    UUID worklogId;
    WorklogDto mockWorklogDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(worklogController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        taskId = UUID.randomUUID();
        worklogId = UUID.randomUUID();

        mockWorklogDto = WorklogDto.builder()
                .id(worklogId)
                .taskId(taskId)
                .workDate(LocalDate.now())
                .hour(BigDecimal.valueOf(4.5))
                .description("Working on feature")
                .createdBy("Developer One")
                .build();
    }

    // ── 1. GET /api/tasks/{taskId}/worklogs ────────────────────────────────────

    @Test
    @DisplayName("GET /api/tasks/{taskId}/worklogs - Should return list of worklogs")
    void getWorklogsByTask_shouldReturnWorklogList() throws Exception {
        PageResponse<WorklogDto> pageResponse = PageResponse.<WorklogDto>builder()
                .items(List.of(mockWorklogDto))
                .totalElements(1L)
                .totalPages(1)
                .pageNumber(0)
                .pageSize(20)
                .build();

        when(worklogService.getWorklogsByTask(eq(taskId), eq(0), eq(20))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/tasks/{taskId}/worklogs", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(worklogId.toString()))
                .andExpect(jsonPath("$.data.items[0].hour").value(4.5));
    }

    // ── 2. POST /api/tasks/{taskId}/worklogs ───────────────────────────────────

    @Test
    @DisplayName("POST /api/tasks/{taskId}/worklogs - Should create worklog when request is valid")
    void createWorklog_validPayload_shouldReturn201() throws Exception {
        CreateWorklogRequest request = new CreateWorklogRequest();
        request.setWorkDate(LocalDate.now());
        request.setHour(BigDecimal.valueOf(4.5));
        request.setDescription("Working on feature");

        when(worklogService.createWorklog(eq(taskId), any(CreateWorklogRequest.class))).thenReturn(mockWorklogDto);

        mockMvc.perform(post("/api/tasks/{taskId}/worklogs", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Worklog created successfully"))
                .andExpect(jsonPath("$.data.id").value(worklogId.toString()));
    }

    // ── 3. PUT /api/worklogs/{id} ──────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/worklogs/{id} - Should update worklog when valid")
    void updateWorklog_validPayload_shouldReturn200() throws Exception {
        UpdateWorklogRequest request = new UpdateWorklogRequest();
        request.setWorkDate(LocalDate.now());
        request.setHour(BigDecimal.valueOf(6.0));
        request.setDescription("Updated worklog");

        when(worklogService.updateWorklog(eq(worklogId), any(UpdateWorklogRequest.class))).thenReturn(mockWorklogDto);

        mockMvc.perform(put("/api/worklogs/{id}", worklogId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Worklog updated successfully"));
    }

    // ── 4. DELETE /api/worklogs/{id} ───────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/worklogs/{id} - Should delete worklog")
    void deleteWorklog_shouldReturn200() throws Exception {
        doNothing().when(worklogService).deleteWorklog(worklogId);

        mockMvc.perform(delete("/api/worklogs/{id}", worklogId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Worklog deleted successfully"));
    }

    // ── 5. GET /api/reports/worklog ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/reports/worklog - Should return worklog report")
    void getWorklogReport_shouldReturnReport() throws Exception {
        WorklogReportItem reportItem = WorklogReportItem.builder()
                .id(worklogId)
                .taskId(taskId)
                .taskKey("WEB-1")
                .taskSummary("Setup Task Test")
                .userId(UUID.randomUUID())
                .userName("Developer One")
                .workDate(LocalDate.now())
                .hour(BigDecimal.valueOf(4.5))
                .description("Working on feature")
                .build();

        PageResponse<WorklogReportItem> pageResponse = PageResponse.<WorklogReportItem>builder()
                .items(List.of(reportItem))
                .totalElements(1L)
                .totalPages(1)
                .pageNumber(0)
                .pageSize(20)
                .build();

        when(worklogService.getWorklogReport(any(WorklogReportFilterDto.class))).thenReturn(pageResponse);

        WorklogReportFilterDto filter = new WorklogReportFilterDto();
        filter.setPage(0);
        filter.setSize(20);

        mockMvc.perform(post("/api/reports/worklog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].taskKey").value("WEB-1"));
    }
}
