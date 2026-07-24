package fpt.qn.pms.activity.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.JSONB;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fpt.qn.pms.activity.dto.DashboardActivityDto;
import fpt.qn.pms.activity.dto.TaskActivityDto;
import fpt.qn.pms.activity.event.TaskActivityEvent;
import fpt.qn.pms.activity.repository.ActivityRepository;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.tables.records.TaskActivitiesRecord;
import fpt.qn.pms.task.repository.TaskRepository;
import fpt.qn.pms.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static fpt.qn.pms.jooq.Tables.USERS;
import org.jooq.DSLContext;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityServiceImpl implements ActivityService {

    ActivityRepository activityRepository;
    TaskRepository taskRepository;
    UserRepository userRepository;
    ObjectMapper objectMapper = new ObjectMapper();
    DSLContext dsl;

    @Override
    @Transactional
    public void logActivity(TaskActivityEvent event) {
        JSONB oldJson = null;
        JSONB newJson = null;
        String message;

        switch (event.action()) {
            case TASK_CREATED -> {
                newJson = toJsonb(Map.of("summary", event.newValue() != null ? event.newValue() : ""));
                message = "created the task";
            }
            case STATUS_CHANGED -> {
                oldJson = event.oldValue() != null ? toJsonb(Map.of("name", event.oldValue())) : null;
                newJson = event.newValue() != null ? toJsonb(Map.of("name", event.newValue())) : null;
                message = String.format("changed status from \"%s\" to \"%s\"", event.oldValue(), event.newValue());
            }
            case PRIORITY_CHANGED -> {
                oldJson = event.oldValue() != null ? toJsonb(Map.of("value", event.oldValue())) : null;
                newJson = event.newValue() != null ? toJsonb(Map.of("value", event.newValue())) : null;
                message = String.format("changed priority from %s to %s", event.oldValue(), event.newValue());
            }
            case ASSIGNEE_CHANGED -> {
                UUID oldUserId = tryParseUuid(event.oldValue());
                UUID newUserId = tryParseUuid(event.newValue());

                List<UUID> uuids = Stream.of(oldUserId, newUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                Map<UUID, String> nameMap = dsl.select(USERS.ID, USERS.FULL_NAME)
                        .from(USERS)
                        .where(USERS.ID.in(uuids))
                        .fetchMap(USERS.ID, USERS.FULL_NAME);

                String oldName = oldUserId != null
                        ? nameMap.getOrDefault(oldUserId, "Unknown") : "Unassigned";
                String newName = newUserId != null
                        ? nameMap.getOrDefault(newUserId, "Unknown") : "Unassigned";

                oldJson = oldUserId != null
                        ? toJsonb(Map.of("id", oldUserId.toString(), "name", oldName))
                        : toJsonb(Map.of("name", "Unassigned"));
                newJson = newUserId != null
                        ? toJsonb(Map.of("id", newUserId.toString(), "name", newName))
                        : toJsonb(Map.of("name", "Unassigned"));

                message = String.format("changed assignee from %s to %s", oldName, newName);
            }
            case COMMENT_ADDED -> {
                boolean isEdit = event.oldValue() != null;
                newJson = event.newValue() != null ? toJsonb(Map.of("content", event.newValue())) : null;
                if (isEdit) {
                    oldJson = toJsonb(Map.of("content", event.oldValue()));
                    message = "edited a comment";
                } else {
                    message = "added a comment";
                }
            }
            case COMMENT_DELETED -> {
                oldJson = event.oldValue() != null ? toJsonb(Map.of("content", event.oldValue())) : null;
                message = "deleted a comment";
            }
            default -> message = "performed an action";
        }

        TaskActivitiesRecord record = new TaskActivitiesRecord();
        record.setTaskId(event.taskId());
        record.setUserId(event.userId());
        record.setAction(event.action());
        record.setOldValue(oldJson);
        record.setNewValue(newJson);
        record.setMessage(message);
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

    @Override
    @Transactional(readOnly = true)
    public List<DashboardActivityDto> getRecentActivities(UUID projectId, int limit) {
        int targetLimit = (limit <= 0 || limit > 50) ? 10 : limit;
        return activityRepository.findRecentActivities(projectId, targetLimit);
    }

    private JSONB toJsonb(Map<String, Object> map) {
        try {
            return JSONB.valueOf(objectMapper.writeValueAsString(map));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize activity value to JSON", e);
        }
    }

    private UUID tryParseUuid(String value) {
        if (value == null) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
