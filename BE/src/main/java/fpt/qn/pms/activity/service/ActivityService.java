package fpt.qn.pms.activity.service;

import java.util.UUID;

import fpt.qn.pms.activity.dto.TaskActivityDto;
import fpt.qn.pms.activity.event.TaskActivityEvent;
import fpt.qn.pms.common.dto.PageResponse;

public interface ActivityService {
    void logActivity(TaskActivityEvent event);
    PageResponse<TaskActivityDto> getActivitiesByTaskId(UUID taskId, int page, int size);
}
