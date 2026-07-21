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
import fpt.qn.pms.sprint.exception.SprintNotFoundException;
import fpt.qn.pms.sprint.mapper.SprintMapper;
import fpt.qn.pms.sprint.repository.SprintRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SprintServiceImpl implements SprintService {

    SprintRepository sprintRepository;
    SprintMapper sprintMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SprintDto> getSprintsByProject(UUID projectId, String keyword, SprintStatus status, int page, int size) {
        PaginationResult<SprintsRecord> result = sprintRepository.findAll(projectId, keyword, status, page, size);
        return PageResponse.<SprintDto>builder()
                .items(result.getItems().stream().map(sprintMapper::toDto).toList())
                .totalElements(result.getTotal())
                .totalPages((int) Math.ceil((double) result.getTotal() / size))
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SprintDto getSprintById(UUID id) {
        return sprintRepository.findById(id)
                .map(sprintMapper::toDto)
                .orElseThrow(() -> new SprintNotFoundException());
    }

    @Override
    @Transactional
    public SprintDto createSprint(CreateSprintRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidDateRangeException();
        }

        SprintsRecord record = sprintMapper.toRecord(request);
        record.setStatus(SprintStatus.PLANNED);

        return sprintMapper.toDto(sprintRepository.create(record));
    }

    @Override
    @Transactional
    public SprintDto updateSprint(UUID id, UpdateSprintRequest request) {
        SprintsRecord record = sprintRepository.findById(id)
                .orElseThrow(() -> new SprintNotFoundException());

        var startDate = request.getStartDate() != null ? request.getStartDate() : record.getStartDate();
        var endDate = request.getEndDate() != null ? request.getEndDate() : record.getEndDate();
        if (endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException();
        }

        sprintMapper.updateRecord(record, request);
        return sprintMapper.toDto(sprintRepository.update(record));
    }

    @Override
    @Transactional
    public SprintDto updateSprintStatus(UUID id, UpdateSprintStatusRequest request) {
        SprintsRecord record = sprintRepository.findById(id)
                .orElseThrow(() -> new SprintNotFoundException());

        SprintStatus currentStatus = record.getStatus();
        SprintStatus newStatus = request.getStatus();

        if (newStatus == SprintStatus.ACTIVE && currentStatus != SprintStatus.ACTIVE
                && sprintRepository.existsActiveByProjectId(record.getProjectId())) {
            throw new ActiveSprintAlreadyExistsException();
        }

        validateStatusTransition(currentStatus, newStatus);

        record.setStatus(newStatus);
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
