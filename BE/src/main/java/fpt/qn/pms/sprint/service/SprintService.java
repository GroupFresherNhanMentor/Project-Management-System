package fpt.qn.pms.sprint.service;

import java.util.UUID;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.sprint.dto.CreateSprintRequest;
import fpt.qn.pms.sprint.dto.SprintDto;
import fpt.qn.pms.sprint.dto.UpdateSprintRequest;
import fpt.qn.pms.sprint.dto.UpdateSprintStatusRequest;

public interface SprintService {

    PageResponse<SprintDto> getSprintsByProject(UUID projectId, String keyword, SprintStatus status, int page, int size);

    SprintDto getSprintById(UUID projectId, UUID id);

    SprintDto createSprint(CreateSprintRequest request);

    SprintDto updateSprint(UUID projectId, UUID id, UpdateSprintRequest request);

    SprintDto updateSprintStatus(UUID projectId, UUID id, UpdateSprintStatusRequest request);
}
