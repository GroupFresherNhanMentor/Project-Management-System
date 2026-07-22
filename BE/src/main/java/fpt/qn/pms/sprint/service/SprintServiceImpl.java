package fpt.qn.pms.sprint.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.jooq.tables.records.SprintsRecord;
import fpt.qn.pms.sprint.dto.CreateSprintRequest;
import fpt.qn.pms.sprint.dto.SprintDto;
import fpt.qn.pms.sprint.dto.UpdateSprintRequest;
import fpt.qn.pms.sprint.dto.UpdateSprintStatusRequest;
import fpt.qn.pms.sprint.exception.ActiveSprintAlreadyExistsException;
import fpt.qn.pms.sprint.exception.InvalidDateRangeException;
import fpt.qn.pms.sprint.exception.InvalidSprintStatusTransitionException;
import fpt.qn.pms.project.exception.ProjectNotFoundException;
import fpt.qn.pms.project.repository.ProjectRepository;
import fpt.qn.pms.sprint.exception.SprintAccessDeniedException;
import fpt.qn.pms.sprint.exception.SprintNotFoundException;
import fpt.qn.pms.sprint.mapper.SprintMapper;
import fpt.qn.pms.sprint.repository.SprintRepository;
import fpt.qn.pms.security.ProjectSecurityEvaluator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SprintServiceImpl implements SprintService {

    SprintRepository sprintRepository;
    SprintMapper sprintMapper;
    ProjectSecurityEvaluator projectSecurityEvaluator;
    ProjectRepository projectRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SprintDto> getSprintsByProject(UUID projectId, String keyword, SprintStatus status, int page, int size) {
        // Check access: ADMIN or project member
        if (!projectSecurityEvaluator.isAdmin() && !projectSecurityEvaluator.isMember(projectId)) {
            throw new SprintAccessDeniedException();
        }
        PaginationResult<SprintsRecord> result = sprintRepository.findAll(projectId, keyword, status, page, size);
        return PageResponse.of(
                result.getItems().stream().map(sprintMapper::toDto).toList(),
                page, size, result.getTotal());
    }

    @Override
    @Transactional(readOnly = true)
    public SprintDto getSprintById(UUID projectId, UUID id) {
        // Check access: ADMIN or project member
        if (!projectSecurityEvaluator.isAdmin() && !projectSecurityEvaluator.isMember(projectId)) {
            throw new SprintAccessDeniedException();
        }
        return sprintRepository.findById(id)
                .filter(record -> record.getProjectId().equals(projectId))
                .map(sprintMapper::toDto)
                .orElseThrow(() -> new SprintNotFoundException());
    }

    @Override
    @Transactional
    public SprintDto createSprint(CreateSprintRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidDateRangeException();
        }

        if (!projectRepository.existsById(request.getProjectId())) {
            throw new ProjectNotFoundException();
        }

        UUID currentUserId = projectSecurityEvaluator.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElseThrow(() -> new SprintAccessDeniedException());

        SprintsRecord record = sprintMapper.toRecord(request);
        record.setStatus(SprintStatus.PLANNED);
        record.setCreatedBy(currentUserId);
        record.setUpdatedBy(currentUserId);

        return sprintMapper.toDto(sprintRepository.create(record));
    }

    @Override
    @Transactional
    public SprintDto updateSprint(UUID projectId, UUID id, UpdateSprintRequest request) {
        SprintsRecord record = sprintRepository.findById(id)
                .orElseThrow(() -> new SprintNotFoundException());

        if (!record.getProjectId().equals(projectId)) {
            throw new SprintNotFoundException();
        }

        var startDate = request.getStartDate() != null ? request.getStartDate() : record.getStartDate();
        var endDate = request.getEndDate() != null ? request.getEndDate() : record.getEndDate();
        if (endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException();
        }

        UUID currentUserId = projectSecurityEvaluator.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElseThrow(() -> new SprintAccessDeniedException());

        sprintMapper.updateRecord(record, request);
        record.setUpdatedBy(currentUserId);
        return sprintMapper.toDto(sprintRepository.update(record));
    }

    @Override
    @Transactional
    public SprintDto updateSprintStatus(UUID projectId, UUID id, UpdateSprintStatusRequest request) {
        SprintsRecord record = sprintRepository.findById(id)
                .orElseThrow(() -> new SprintNotFoundException());

        if (!record.getProjectId().equals(projectId)) {
            throw new SprintNotFoundException();
        }

        SprintStatus currentStatus = record.getStatus();
        SprintStatus newStatus = request.getStatus();

        if (newStatus == SprintStatus.ACTIVE && currentStatus != SprintStatus.ACTIVE
                && sprintRepository.existsActiveByProjectId(record.getProjectId())) {
            throw new ActiveSprintAlreadyExistsException();
        }

        validateStatusTransition(currentStatus, newStatus);

        UUID currentUserId = projectSecurityEvaluator.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElseThrow(() -> new SprintAccessDeniedException());

        record.setStatus(newStatus);
        record.setUpdatedBy(currentUserId);
        return sprintMapper.toDto(sprintRepository.update(record));
    }

    private void validateStatusTransition(SprintStatus current, SprintStatus target) {
        boolean valid = switch (current) {
            case PLANNED -> target == SprintStatus.ACTIVE;
            case ACTIVE -> target == SprintStatus.CLOSED;
            case CLOSED -> false;
        };

        if (!valid) {
            throw new InvalidSprintStatusTransitionException(
                    "Invalid status transition: " + current.getLiteral() + " -> " + target.getLiteral());
        }
    }
}
