package fpt.qn.pms.task.service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.jooq.tables.records.TaskWorkflowRecord;
import fpt.qn.pms.task.dto.CreateTaskWorkflowRequest;
import fpt.qn.pms.task.dto.TaskWorkflowDto;
import fpt.qn.pms.task.exception.FinalStatusCannotHaveOutgoingTransitionException;
import fpt.qn.pms.task.exception.TaskStatusNotFoundException;
import fpt.qn.pms.task.exception.TaskWorkflowAlreadyExistsException;
import fpt.qn.pms.task.exception.TaskWorkflowNotFoundException;
import fpt.qn.pms.task.exception.TaskWorkflowSelfLoopException;
import fpt.qn.pms.task.mapper.TaskWorkflowMapper;
import fpt.qn.pms.task.repository.TaskStatusRepository;
import fpt.qn.pms.task.repository.TaskWorkflowRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskWorkflowServiceImpl implements TaskWorkflowService {

    TaskWorkflowRepository taskWorkflowRepository;
    TaskStatusRepository taskStatusRepository;
    TaskWorkflowMapper taskWorkflowMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TaskWorkflowDto> getByProject(UUID projectId) {
        return taskWorkflowRepository.findAllByProjectId(projectId)
                .stream()
                .map(taskWorkflowMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public List<TaskWorkflowDto> createAll(UUID projectId, List<CreateTaskWorkflowRequest> requests) {
        // collect project's status IDs once to avoid per-item DB calls
        var projectStatusIds = taskStatusRepository.findAllByProjectId(projectId, null, null)
                .stream()
                .map(r -> r.getId())
                .collect(Collectors.toSet());

        // validate all items up front — fail the whole batch on first violation
        var seen = new HashSet<String>();
        for (var req : requests) {
            if (req.getFromStatusId().equals(req.getToStatusId())) {
                throw new TaskWorkflowSelfLoopException();
            }
            if (!projectStatusIds.contains(req.getFromStatusId())) {
                throw new TaskStatusNotFoundException(req.getFromStatusId());
            }
            if (!projectStatusIds.contains(req.getToStatusId())) {
                throw new TaskStatusNotFoundException(req.getToStatusId());
            }
            boolean fromStatusIsFinal = taskStatusRepository.findById(req.getFromStatusId())
                    .map(s -> Boolean.TRUE.equals(s.getIsFinal()))
                    .orElse(false);
            if (fromStatusIsFinal) {
                throw new FinalStatusCannotHaveOutgoingTransitionException();
            }
            String key = req.getFromStatusId() + "->" + req.getToStatusId();
            if (!seen.add(key)) {
                throw new TaskWorkflowAlreadyExistsException();
            }
            if (taskWorkflowRepository.existsByFromStatusIdAndToStatusId(req.getFromStatusId(), req.getToStatusId())) {
                throw new TaskWorkflowAlreadyExistsException();
            }
        }

        List<TaskWorkflowRecord> records = requests.stream()
                .map(taskWorkflowMapper::toRecord)
                .toList();

        return taskWorkflowRepository.createAll(records)
                .stream()
                .map(taskWorkflowMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID projectId, UUID id) {
        taskWorkflowRepository.findByIdAndProjectId(id, projectId)
                .orElseThrow(() -> new TaskWorkflowNotFoundException(id));

        taskWorkflowRepository.hardDeleteById(id);
    }
}
