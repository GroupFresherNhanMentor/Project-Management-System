package fpt.qn.pms.projectmember.repository;

import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.USERS;
import static org.jooq.impl.DSL.notExists;
import static org.jooq.impl.DSL.selectOne;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.ProjectMembersRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.projectmember.repository.projection.ProjectMemberDetails;

@Repository
public class ProjectMemberRepositoryImpl extends BaseRepository<ProjectMembersRecord> implements ProjectMemberRepository {

    public ProjectMemberRepositoryImpl(DSLContext dsl) {
        super(dsl, PROJECT_MEMBERS);
    }

    @Override
    public Optional<ProjectMembersRecord> findByProjectIdAndUserId(UUID projectId, UUID userId) {
        return dsl.selectFrom(PROJECT_MEMBERS)
                .where(PROJECT_MEMBERS.PROJECT_ID.eq(projectId))
                .and(PROJECT_MEMBERS.USER_ID.eq(userId))
                .fetchOptional();
    }

    @Override
    public Optional<ProjectMembersRecord> findByIdAndProjectId(UUID id, UUID projectId) {
        return dsl.selectFrom(PROJECT_MEMBERS)
                .where(PROJECT_MEMBERS.ID.eq(id))
                .and(PROJECT_MEMBERS.PROJECT_ID.eq(projectId))
                .fetchOptional();
    }

    @Override
    public Optional<ProjectMemberDetails> findDetailsByIdAndProjectId(UUID id, UUID projectId) {
        return detailsQuery(projectId)
                .and(PROJECT_MEMBERS.ID.eq(id))
                .fetchOptional(this::toDetails);
    }

    @Override
    public PaginationResult<ProjectMemberDetails> findAllByProjectId(UUID projectId, int page, int size) {
        long total = dsl.fetchCount(PROJECT_MEMBERS, PROJECT_MEMBERS.PROJECT_ID.eq(projectId));

        List<ProjectMemberDetails> items = detailsQuery(projectId)
                .orderBy(PROJECT_MEMBERS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch(this::toDetails);

        return new PaginationResult<>(total, items);
    }

    @Override
    public PaginationResult<UsersRecord> findAvailableUsers(
            UUID projectId, String keyword, int page, int size) {
        Condition condition = USERS.ROLE.eq(SysRole.USER)
                .and(USERS.STATUS.eq(UserStatus.ACTIVE))
                .and(notExists(selectOne()
                        .from(PROJECT_MEMBERS)
                        .where(PROJECT_MEMBERS.PROJECT_ID.eq(projectId))
                        .and(PROJECT_MEMBERS.USER_ID.eq(USERS.ID))
                        .and(PROJECT_MEMBERS.STATUS.eq(ProjectMemberStatus.ACTIVE))));

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim() + "%";
            condition = condition.and(
                    USERS.EMPLOYEE_ID.likeIgnoreCase(pattern)
                            .or(USERS.FULL_NAME.likeIgnoreCase(pattern))
                            .or(USERS.EMAIL.likeIgnoreCase(pattern)));
        }

        long total = dsl.fetchCount(USERS, condition);
        List<UsersRecord> items = dsl.selectFrom(USERS)
                .where(condition)
                .orderBy(USERS.FULL_NAME.asc(), USERS.EMPLOYEE_ID.asc())
                .limit(size)
                .offset((long) page * size)
                .fetch();

        return new PaginationResult<>(total, items);
    }

    @Override
    public boolean existsActiveByProjectIdAndUserId(UUID projectId, UUID userId) {
        return dsl.fetchExists(PROJECT_MEMBERS,
                PROJECT_MEMBERS.PROJECT_ID.eq(projectId)
                        .and(PROJECT_MEMBERS.USER_ID.eq(userId))
                        .and(PROJECT_MEMBERS.STATUS.eq(ProjectMemberStatus.ACTIVE)));
    }

    @Override
    public boolean existsActiveByProjectIdAndUserIdAndRole(UUID projectId, UUID userId, ProjectRole role) {
        return dsl.fetchExists(PROJECT_MEMBERS,
                PROJECT_MEMBERS.PROJECT_ID.eq(projectId)
                        .and(PROJECT_MEMBERS.USER_ID.eq(userId))
                        .and(PROJECT_MEMBERS.PROJECT_ROLE.eq(role))
                        .and(PROJECT_MEMBERS.STATUS.eq(ProjectMemberStatus.ACTIVE)));
    }

    @Override
    public long countActiveByProjectIdAndRole(UUID projectId, ProjectRole role) {
        return dsl.fetchCount(PROJECT_MEMBERS,
                PROJECT_MEMBERS.PROJECT_ID.eq(projectId)
                        .and(PROJECT_MEMBERS.PROJECT_ROLE.eq(role))
                        .and(PROJECT_MEMBERS.STATUS.eq(ProjectMemberStatus.ACTIVE)));
    }

    private org.jooq.SelectConditionStep<? extends org.jooq.Record> detailsQuery(UUID projectId) {
        return dsl.select(
                        PROJECT_MEMBERS.ID,
                        PROJECT_MEMBERS.PROJECT_ID,
                        PROJECT_MEMBERS.USER_ID,
                        USERS.EMPLOYEE_ID,
                        USERS.FULL_NAME,
                        USERS.EMAIL,
                        PROJECT_MEMBERS.PROJECT_ROLE,
                        PROJECT_MEMBERS.STATUS,
                        PROJECT_MEMBERS.CREATED_AT)
                .from(PROJECT_MEMBERS)
                .join(USERS).on(USERS.ID.eq(PROJECT_MEMBERS.USER_ID))
                .where(PROJECT_MEMBERS.PROJECT_ID.eq(projectId));
    }

    private ProjectMemberDetails toDetails(org.jooq.Record record) {
        return new ProjectMemberDetails(
                record.get(PROJECT_MEMBERS.ID),
                record.get(PROJECT_MEMBERS.PROJECT_ID),
                record.get(PROJECT_MEMBERS.USER_ID),
                record.get(USERS.EMPLOYEE_ID),
                record.get(USERS.FULL_NAME),
                record.get(USERS.EMAIL),
                record.get(PROJECT_MEMBERS.PROJECT_ROLE),
                record.get(PROJECT_MEMBERS.STATUS),
                record.get(PROJECT_MEMBERS.CREATED_AT));
    }
}
