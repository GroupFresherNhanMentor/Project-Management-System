package fpt.qn.pms.project.repository;

import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.PROJECTS;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;

@Repository
public class ProjectRepositoryImpl extends BaseRepository<ProjectsRecord> implements ProjectRepository {

    public ProjectRepositoryImpl(DSLContext dsl) {
        super(dsl, PROJECTS);
    }

    @Override
    public boolean existsByProjectCodeIgnoreCase(String projectCode) {
        return dsl.fetchExists(PROJECTS,
                DSL.lower(PROJECTS.PROJECT_CODE).eq(projectCode.toLowerCase(Locale.ROOT)));
    }

    @Override
    public boolean lockById(UUID projectId) {
        return dsl.select(PROJECTS.ID)
                .from(PROJECTS)
                .where(PROJECTS.ID.eq(projectId))
                .forUpdate()
                .fetchOptional()
                .isPresent();
    }

    @Override
    public PaginationResult<ProjectsRecord> findAll(
            String keyword, ProjectStatus status, UUID memberUserId, int page, int size) {
        Condition condition = buildCondition(keyword, status, memberUserId);
        long total = dsl.fetchCount(PROJECTS, condition);

        List<ProjectsRecord> items = dsl.selectFrom(PROJECTS)
                .where(condition)
                .orderBy(PROJECTS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch();

        return new PaginationResult<>(total, items);
    }

    private Condition buildCondition(String keyword, ProjectStatus status, UUID memberUserId) {
        Condition condition = DSL.noCondition();

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim() + "%";
            condition = condition.and(
                    PROJECTS.PROJECT_CODE.likeIgnoreCase(pattern)
                            .or(PROJECTS.PROJECT_NAME.likeIgnoreCase(pattern)));
        }

        if (status != null) {
            condition = condition.and(PROJECTS.STATUS.eq(status));
        }

        if (memberUserId != null) {
            condition = condition.andExists(
                    DSL.selectOne()
                            .from(PROJECT_MEMBERS)
                            .where(PROJECT_MEMBERS.PROJECT_ID.eq(PROJECTS.ID))
                            .and(PROJECT_MEMBERS.USER_ID.eq(memberUserId))
                            .and(PROJECT_MEMBERS.STATUS.eq(ProjectMemberStatus.ACTIVE)));
        }

        return condition;
    }
}
