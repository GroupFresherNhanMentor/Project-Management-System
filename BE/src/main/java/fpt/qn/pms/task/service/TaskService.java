package fpt.qn.pms.task.service;

import java.util.UUID;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.task.dto.AssignTaskRequest;
import fpt.qn.pms.task.dto.CreateTaskRequest;
import fpt.qn.pms.task.dto.TaskDto;
import fpt.qn.pms.task.dto.TaskSearchRequest;
import fpt.qn.pms.task.dto.UpdateTaskRequest;

public interface TaskService {

    TaskDto createTask(CreateTaskRequest request);

    TaskDto getTaskById(UUID id);

    PageResponse<TaskDto> searchTasks(TaskSearchRequest request);

    TaskDto updateTask(UUID id, UpdateTaskRequest request);

    TaskDto assignTask(UUID id, AssignTaskRequest request);
}
