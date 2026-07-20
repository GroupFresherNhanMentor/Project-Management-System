package fpt.qn.pms.sprint.repository;

import static fpt.qn.pms.jooq.Tables.SPRINTS;

import java.util.List;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.jooq.tables.records.SprintsRecord;

@Repository
public class SprintRepositoryImpl extends BaseRepository<SprintsRecord> implements SprintRepository {

    public SprintRepositoryImpl(DSLContext dsl) {
        super(dsl, SPRINTS);
    }

    @Override
    public PaginationResult<SprintsRecord> findAll(UUID projectId, String keyword, SprintStatus status, int page, int size) {
        Condition condition = buildCondition(projectId, keyword, status);

        long total = dsl.fetchCount(SPRINTS, condition);

        List<SprintsRecord> items = dsl.selectFrom(SPRINTS)
                .where(condition)
                .orderBy(SPRINTS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch();

        return new PaginationResult<>(total, items);
    }

    @Override
    public boolean existsActiveByProjectId(UUID projectId) {
        return dsl.fetchExists(SPRINTS,
                SPRINTS.PROJECT_ID.eq(projectId)
                        .and(SPRINTS.STATUS.eq(SprintStatus.ACTIVE)));
    }

    private Condition buildCondition(UUID projectId, String keyword, SprintStatus status) {
        Condition condition = DSL.noCondition();

        condition = condition.and(SPRINTS.PROJECT_ID.eq(projectId));

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            condition = condition.and(
                    SPRINTS.SPRINT_NAME.likeIgnoreCase(pattern)
            );
        }
        if (status != null) {
            condition = condition.and(SPRINTS.STATUS.eq(status));
        }

        return condition;
    }
}
