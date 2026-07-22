package fpt.qn.pms.task.service;

import java.util.List;
import java.util.UUID;


import fpt.qn.pms.task.dto.CreateTaskWorkflowRequest;
import fpt.qn.pms.task.dto.TaskWorkflowDto;

public interface TaskWorkflowService {

    List<TaskWorkflowDto> getByProject(UUID projectId);

    List<TaskWorkflowDto> createAll(UUID projectId, List<CreateTaskWorkflowRequest> requests);

    void delete(UUID projectId, UUID id);
}
