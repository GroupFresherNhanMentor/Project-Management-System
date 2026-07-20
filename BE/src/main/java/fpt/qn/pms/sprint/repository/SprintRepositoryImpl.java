package fpt.qn.pms.sprint.repository;

import static fpt.qn.pms.jooq.Tables.SPRINTS;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.records.SprintsRecord;

@Repository
public class SprintRepositoryImpl extends BaseRepository<SprintsRecord> implements SprintRepository {

    public SprintRepositoryImpl(DSLContext dsl) {
        super(dsl, SPRINTS);
    }
}
