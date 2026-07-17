package fpt.qn.project_management_system.sprint.repository;

import static fpt.qn.project_management_system.jooq.Tables.SPRINTS;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.project_management_system.common.repository.BaseRepository;
import fpt.qn.project_management_system.jooq.tables.records.SprintsRecord;

@Repository
public class SprintRepositoryImpl extends BaseRepository<SprintsRecord> implements SprintRepository {

    public SprintRepositoryImpl(DSLContext dsl) {
        super(dsl, SPRINTS);
    }
}
