package fpt.qn.pms.activity.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.activity.dto.TaskActivityDto;
import fpt.qn.pms.activity.event.TaskActivityEvent;
import fpt.qn.pms.activity.repository.ActivityRepository;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import org.springframework.http.HttpStatus;
import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.tables.records.TaskActivitiesRecord;
import fpt.qn.pms.task.repository.TaskRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityServiceImpl implements ActivityService {

    ActivityRepository activityRepository;
    TaskRepository taskRepository;

    @Override
    @Transactional
    public void logActivity(TaskActivityEvent event) {
        TaskActivitiesRecord record = new TaskActivitiesRecord();
        record.setTaskId(event.taskId());
        record.setUserId(event.userId());
        record.setAction(event.action());
        record.setOldValue(event.oldValue());
        record.setNewValue(event.newValue());
        activityRepository.create(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskActivityDto> getActivitiesByTaskId(UUID taskId, int page, int size) {
        if (!taskRepository.existsById(taskId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId);
        }

        PaginationResult<TaskActivityDto> result = activityRepository.findByTaskId(taskId, page, size);
        int totalPages = (int) Math.ceil((double) result.getTotal() / size);

        return PageResponse.<TaskActivityDto>builder()
                .totalElements(result.getTotal())
                .totalPages(totalPages)
                .pageNumber(page)
                .pageSize(size)
                .items(result.getItems())
                .build();
    }
}
