package fpt.qn.pms.task.service;

import java.util.List;
import java.util.UUID;

import fpt.qn.pms.task.dto.CreateTaskStatusRequest;
import fpt.qn.pms.task.dto.TaskStatusDto;
import fpt.qn.pms.task.dto.UpdateTaskStatusRequest;

public interface TaskStatusService {

    List<TaskStatusDto> getByProject(UUID projectId, Boolean isInitial, Boolean isActive);

    TaskStatusDto create(UUID projectId, CreateTaskStatusRequest request);

    TaskStatusDto update(UUID projectId, UUID id, UpdateTaskStatusRequest request);

    void delete(UUID projectId, UUID id);
}
