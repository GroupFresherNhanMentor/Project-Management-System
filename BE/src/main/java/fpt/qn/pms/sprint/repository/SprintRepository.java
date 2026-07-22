package fpt.qn.pms.sprint.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.jooq.tables.records.SprintsRecord;

public interface SprintRepository extends Repository<SprintsRecord> {

    PaginationResult<SprintsRecord> findAll(UUID projectId, String keyword, SprintStatus status, int page, int size);

    boolean existsActiveByProjectId(UUID projectId);

    List<SprintsRecord> findActiveSprintsPastEndDate(LocalDate date);
}
