package fpt.qn.pms.project.repository;

import java.util.UUID;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;

public interface ProjectRepository extends Repository<ProjectsRecord> {

    boolean existsByProjectCodeIgnoreCase(String projectCode);

    PaginationResult<ProjectsRecord> findAll(
            String keyword, ProjectStatus status, UUID memberUserId, int page, int size);
}
