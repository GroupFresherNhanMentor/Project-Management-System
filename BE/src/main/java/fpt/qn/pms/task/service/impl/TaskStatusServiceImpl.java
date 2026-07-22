package fpt.qn.pms.task.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.jooq.tables.records.TaskStatusesRecord;
import fpt.qn.pms.task.dto.CreateTaskStatusRequest;
import fpt.qn.pms.task.dto.TaskStatusDto;
import fpt.qn.pms.task.dto.UpdateTaskStatusRequest;
import fpt.qn.pms.task.exception.InvalidTaskStatusFlagsException;
import fpt.qn.pms.task.exception.TaskStatusNameConflictException;
import fpt.qn.pms.task.exception.TaskStatusNotFoundException;
import fpt.qn.pms.task.mapper.TaskStatusMapper;
import fpt.qn.pms.task.repository.TaskStatusRepository;
import fpt.qn.pms.task.service.TaskStatusService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskStatusServiceImpl implements TaskStatusService {

    TaskStatusRepository taskStatusRepository;
    TaskStatusMapper taskStatusMapper;

    @Override
    @Transactional(readOnly = true)

    public List<TaskStatusDto> getByProject(UUID projectId, Boolean isInitial, Boolean isActive) {
        return taskStatusRepository.findAllByProjectId(projectId, isInitial, isActive)
                .stream()
                .map(taskStatusMapper::toDto)
                .toList();
    }

    @Override
    @Transactional

    public TaskStatusDto create(UUID projectId, CreateTaskStatusRequest request) {
        if (Boolean.TRUE.equals(request.getIsInitial()) && Boolean.TRUE.equals(request.getIsFinal())) {
            throw new InvalidTaskStatusFlagsException();
        }

        if (taskStatusRepository.existsByProjectIdAndName(projectId, request.getName())) {
            throw new TaskStatusNameConflictException(request.getName());
        }

        TaskStatusesRecord record = taskStatusMapper.toRecord(request);
        record.setProjectId(projectId);

        return taskStatusMapper.toDto(taskStatusRepository.create(record));
    }

    @Override
    @Transactional

    public TaskStatusDto update(UUID projectId, UUID id, UpdateTaskStatusRequest request) {
        TaskStatusesRecord record = taskStatusRepository.findById(id)
                .filter(r -> r.getProjectId().equals(projectId))
                .orElseThrow(() -> new TaskStatusNotFoundException(id));

        boolean effectiveIsInitial = request.getIsInitial() != null ? request.getIsInitial() : record.getIsInitial();
        boolean effectiveIsFinal = request.getIsFinal() != null ? request.getIsFinal() : record.getIsFinal();
        if (effectiveIsInitial && effectiveIsFinal) {
            throw new InvalidTaskStatusFlagsException();
        }

        if (request.getName() != null
                && taskStatusRepository.existsByProjectIdAndNameAndIdNot(projectId, request.getName(), id)) {
            throw new TaskStatusNameConflictException(request.getName());
        }

        taskStatusMapper.updateRecord(record, request);
        return taskStatusMapper.toDto(taskStatusRepository.update(record));
    }

    @Override
    @Transactional

    public void delete(UUID projectId, UUID id) {
        taskStatusRepository.findById(id)
                .filter(r -> r.getProjectId().equals(projectId))
                .orElseThrow(() -> new TaskStatusNotFoundException(id));

        taskStatusRepository.softDeleteById(id);
    }
}
