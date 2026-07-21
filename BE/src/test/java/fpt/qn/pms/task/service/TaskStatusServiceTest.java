package fpt.qn.pms.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import fpt.qn.pms.jooq.tables.records.TaskStatusesRecord;
import fpt.qn.pms.task.dto.CreateTaskStatusRequest;
import fpt.qn.pms.task.dto.TaskStatusDto;
import fpt.qn.pms.task.dto.UpdateTaskStatusRequest;
import fpt.qn.pms.task.exception.InvalidTaskStatusFlagsException;
import fpt.qn.pms.task.exception.TaskStatusNameConflictException;
import fpt.qn.pms.task.exception.TaskStatusNotFoundException;
import fpt.qn.pms.task.mapper.TaskStatusMapper;
import fpt.qn.pms.task.repository.TaskStatusRepository;

@ExtendWith(MockitoExtension.class)
class TaskStatusServiceTest {

    @Mock
    TaskStatusRepository taskStatusRepository;

    @Mock
    TaskStatusMapper taskStatusMapper;

    @InjectMocks
    TaskStatusServiceImpl taskStatusService;

    UUID projectId;
    UUID statusId;
    TaskStatusesRecord mockRecord;
    TaskStatusDto mockDto;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        statusId = UUID.randomUUID();

        mockRecord = new TaskStatusesRecord();
        mockRecord.setId(statusId);
        mockRecord.setProjectId(projectId);
        mockRecord.setName("In Progress");
        mockRecord.setColor("#3B4FD9");
        mockRecord.setIsInitial(false);
        mockRecord.setIsFinal(false);

        mockDto = TaskStatusDto.builder()
                .id(statusId)
                .projectId(projectId)
                .name("In Progress")
                .color("#3B4FD9")
                .isInitial(false)
                .isFinal(false)
                .build();
    }

    // ── 1. Get By Project ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getByProject - Should return all statuses when isInitial filter is null")
    void getByProject_noFilter() {
        when(taskStatusRepository.findAllByProjectId(projectId, null, null)).thenReturn(List.of(mockRecord));
        when(taskStatusMapper.toDto(mockRecord)).thenReturn(mockDto);

        List<TaskStatusDto> result = taskStatusService.getByProject(projectId, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(statusId);
        assertThat(result.get(0).getName()).isEqualTo("In Progress");
    }

    @Test
    @DisplayName("getByProject - Should return only initial statuses when isInitial=true")
    void getByProject_filterInitial() {
        TaskStatusDto initialDto = TaskStatusDto.builder()
                .id(statusId)
                .projectId(projectId)
                .name("To Do")
                .isInitial(true)
                .isFinal(false)
                .build();

        when(taskStatusRepository.findAllByProjectId(projectId, true, null)).thenReturn(List.of(mockRecord));
        when(taskStatusMapper.toDto(mockRecord)).thenReturn(initialDto);

        List<TaskStatusDto> result = taskStatusService.getByProject(projectId, true, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsInitial()).isTrue();
    }

    @Test
    @DisplayName("getByProject - Should return empty list when project has no statuses")
    void getByProject_empty() {
        when(taskStatusRepository.findAllByProjectId(projectId, null, null)).thenReturn(List.of());

        List<TaskStatusDto> result = taskStatusService.getByProject(projectId, null, null);

        assertThat(result).isEmpty();
    }

    // ── 2. Create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create - Should create and return TaskStatusDto")
    void create_success() {
        CreateTaskStatusRequest request = new CreateTaskStatusRequest();
        request.setName("In Progress");
        request.setColor("#3B4FD9");
        request.setIsInitial(false);
        request.setIsFinal(false);

        when(taskStatusRepository.existsByProjectIdAndName(projectId, "In Progress")).thenReturn(false);
        when(taskStatusMapper.toRecord(request)).thenReturn(mockRecord);
        when(taskStatusRepository.create(any(TaskStatusesRecord.class))).thenReturn(mockRecord);
        when(taskStatusMapper.toDto(mockRecord)).thenReturn(mockDto);

        TaskStatusDto result = taskStatusService.create(projectId, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("In Progress");
        assertThat(result.getProjectId()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("create - Should throw InvalidTaskStatusFlagsException when both isInitial and isFinal are true")
    void create_bothInitialAndFinal_shouldThrowException() {
        CreateTaskStatusRequest request = new CreateTaskStatusRequest();
        request.setName("Invalid");
        request.setIsInitial(true);
        request.setIsFinal(true);

        assertThatThrownBy(() -> taskStatusService.create(projectId, request))
                .isInstanceOf(InvalidTaskStatusFlagsException.class)
                .hasMessageContaining("cannot be both initial and final");
    }

    @Test
    @DisplayName("create - Should throw TaskStatusNameConflictException when name already exists in project")
    void create_duplicateName_shouldThrowException() {
        CreateTaskStatusRequest request = new CreateTaskStatusRequest();
        request.setName("In Progress");

        when(taskStatusRepository.existsByProjectIdAndName(projectId, "In Progress")).thenReturn(true);

        assertThatThrownBy(() -> taskStatusService.create(projectId, request))
                .isInstanceOf(TaskStatusNameConflictException.class)
                .hasMessageContaining("In Progress");
    }

    // ── 3. Update ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update - Should update and return updated TaskStatusDto")
    void update_success() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setName("Done");
        request.setColor("#00C853");

        TaskStatusDto updatedDto = TaskStatusDto.builder()
                .id(statusId)
                .projectId(projectId)
                .name("Done")
                .color("#00C853")
                .build();

        when(taskStatusRepository.findById(statusId)).thenReturn(Optional.of(mockRecord));
        when(taskStatusRepository.existsByProjectIdAndNameAndIdNot(projectId, "Done", statusId)).thenReturn(false);
        when(taskStatusRepository.update(mockRecord)).thenReturn(mockRecord);
        when(taskStatusMapper.toDto(mockRecord)).thenReturn(updatedDto);

        TaskStatusDto result = taskStatusService.update(projectId, statusId, request);

        assertThat(result.getName()).isEqualTo("Done");
        assertThat(result.getColor()).isEqualTo("#00C853");
    }

    @Test
    @DisplayName("update - Should throw TaskStatusNotFoundException when status does not belong to project")
    void update_wrongProject_shouldThrowException() {
        UUID otherProjectId = UUID.randomUUID();
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setName("Done");

        when(taskStatusRepository.findById(statusId)).thenReturn(Optional.of(mockRecord));

        assertThatThrownBy(() -> taskStatusService.update(otherProjectId, statusId, request))
                .isInstanceOf(TaskStatusNotFoundException.class)
                .hasMessageContaining(statusId.toString());
    }

    @Test
    @DisplayName("update - Should throw TaskStatusNotFoundException when status does not exist")
    void update_notFound_shouldThrowException() {
        UUID nonExistingId = UUID.randomUUID();
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setName("Done");

        when(taskStatusRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskStatusService.update(projectId, nonExistingId, request))
                .isInstanceOf(TaskStatusNotFoundException.class)
                .hasMessageContaining(nonExistingId.toString());
    }

    @Test
    @DisplayName("update - Should throw InvalidTaskStatusFlagsException when effective state would be both initial and final")
    void update_bothInitialAndFinal_shouldThrowException() {
        mockRecord.setIsInitial(true);
        mockRecord.setIsFinal(false);

        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setIsFinal(true); // isInitial stays true from existing record

        when(taskStatusRepository.findById(statusId)).thenReturn(Optional.of(mockRecord));

        assertThatThrownBy(() -> taskStatusService.update(projectId, statusId, request))
                .isInstanceOf(InvalidTaskStatusFlagsException.class)
                .hasMessageContaining("cannot be both initial and final");
    }

    @Test
    @DisplayName("update - Should throw TaskStatusNameConflictException when new name conflicts with another status")
    void update_duplicateName_shouldThrowException() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setName("Done");

        when(taskStatusRepository.findById(statusId)).thenReturn(Optional.of(mockRecord));
        when(taskStatusRepository.existsByProjectIdAndNameAndIdNot(projectId, "Done", statusId)).thenReturn(true);

        assertThatThrownBy(() -> taskStatusService.update(projectId, statusId, request))
                .isInstanceOf(TaskStatusNameConflictException.class)
                .hasMessageContaining("Done");
    }

    // ── 4. Delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete - Should soft-delete task status by setting is_active to false")
    void delete_success() {
        when(taskStatusRepository.findById(statusId)).thenReturn(Optional.of(mockRecord));

        taskStatusService.delete(projectId, statusId);

        verify(taskStatusRepository).softDeleteById(statusId);
    }

    @Test
    @DisplayName("delete - Should throw TaskStatusNotFoundException when status does not exist")
    void delete_notFound_shouldThrowException() {
        UUID nonExistingId = UUID.randomUUID();
        when(taskStatusRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskStatusService.delete(projectId, nonExistingId))
                .isInstanceOf(TaskStatusNotFoundException.class)
                .hasMessageContaining(nonExistingId.toString());
    }

    @Test
    @DisplayName("delete - Should throw TaskStatusNotFoundException when status does not belong to project")
    void delete_wrongProject_shouldThrowException() {
        UUID otherProjectId = UUID.randomUUID();
        when(taskStatusRepository.findById(statusId)).thenReturn(Optional.of(mockRecord));

        assertThatThrownBy(() -> taskStatusService.delete(otherProjectId, statusId))
                .isInstanceOf(TaskStatusNotFoundException.class)
                .hasMessageContaining(statusId.toString());
    }
}
