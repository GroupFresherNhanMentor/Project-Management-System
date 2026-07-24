package fpt.qn.pms.activity.repository;

import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.TASKS;
import static fpt.qn.pms.jooq.Tables.TASK_ACTIVITIES;
import static fpt.qn.pms.jooq.Tables.USERS;

import java.util.List;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.activity.dto.DashboardActivityDto;
import fpt.qn.pms.activity.dto.TaskActivityDto;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.tables.records.TaskActivitiesRecord;

@Repository
public class ActivityRepositoryImpl extends BaseRepository<TaskActivitiesRecord> implements ActivityRepository {

    public ActivityRepositoryImpl(DSLContext dsl) {
        super(dsl, TASK_ACTIVITIES);
    }

    @Override
    public PaginationResult<TaskActivityDto> findByTaskId(UUID taskId, int page, int size) {
        long total = dsl.fetchCount(TASK_ACTIVITIES, TASK_ACTIVITIES.TASK_ID.eq(taskId));

        List<TaskActivityDto> items = dsl.select(
                    TASK_ACTIVITIES.ID,
                    TASK_ACTIVITIES.TASK_ID,
                    TASK_ACTIVITIES.USER_ID,
                    USERS.FULL_NAME,
                    TASK_ACTIVITIES.ACTION,
                    TASK_ACTIVITIES.OLD_VALUE,
                    TASK_ACTIVITIES.NEW_VALUE,
                    TASK_ACTIVITIES.MESSAGE,
                    TASK_ACTIVITIES.CREATED_AT
                )
                .from(TASK_ACTIVITIES)
                .leftJoin(USERS).on(TASK_ACTIVITIES.USER_ID.eq(USERS.ID))
                .where(TASK_ACTIVITIES.TASK_ID.eq(taskId))
                .orderBy(TASK_ACTIVITIES.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch(r -> {
                    var oldVal = r.get(TASK_ACTIVITIES.OLD_VALUE);
                    var newVal = r.get(TASK_ACTIVITIES.NEW_VALUE);
                    return TaskActivityDto.builder()
                            .id(r.get(TASK_ACTIVITIES.ID))
                            .taskId(r.get(TASK_ACTIVITIES.TASK_ID))
                            .userId(r.get(TASK_ACTIVITIES.USER_ID))
                            .userName(r.get(USERS.FULL_NAME))
                            .action(r.get(TASK_ACTIVITIES.ACTION))
                            .oldValue(oldVal != null ? oldVal.data() : null)
                            .newValue(newVal != null ? newVal.data() : null)
                            .message(r.get(TASK_ACTIVITIES.MESSAGE))
                            .createdTime(r.get(TASK_ACTIVITIES.CREATED_AT))
                            .build();
                });

        return new PaginationResult<>(total, items);
    }

    @Override
    public List<DashboardActivityDto> findRecentActivities(UUID projectId, UUID userId, boolean isAdmin, int limit) {
        Condition condition = DSL.noCondition();
        if (projectId != null) {
            condition = TASKS.PROJECT_ID.eq(projectId);
        } else if (!isAdmin && userId != null) {
            condition = TASKS.PROJECT_ID.in(
                    DSL.select(PROJECT_MEMBERS.PROJECT_ID)
                            .from(PROJECT_MEMBERS)
                            .where(PROJECT_MEMBERS.USER_ID.eq(userId))
                            .and(PROJECT_MEMBERS.STATUS.eq(ProjectMemberStatus.ACTIVE))
            );
        }

        return dsl.select(
                    TASK_ACTIVITIES.ID,
                    TASK_ACTIVITIES.TASK_ID,
                    TASKS.TASK_KEY,
                    TASKS.PROJECT_ID,
                    PROJECTS.PROJECT_CODE,
                    PROJECTS.PROJECT_NAME,
                    TASK_ACTIVITIES.USER_ID,
                    USERS.FULL_NAME,
                    TASK_ACTIVITIES.ACTION,
                    TASK_ACTIVITIES.MESSAGE,
                    TASK_ACTIVITIES.CREATED_AT
                )
                .from(TASK_ACTIVITIES)
                .innerJoin(TASKS).on(TASK_ACTIVITIES.TASK_ID.eq(TASKS.ID))
                .innerJoin(PROJECTS).on(TASKS.PROJECT_ID.eq(PROJECTS.ID))
                .leftJoin(USERS).on(TASK_ACTIVITIES.USER_ID.eq(USERS.ID))
                .where(condition)
                .orderBy(TASK_ACTIVITIES.CREATED_AT.desc())
                .limit(limit)
                .fetch(r -> DashboardActivityDto.builder()
                        .id(r.get(TASK_ACTIVITIES.ID))
                        .taskId(r.get(TASK_ACTIVITIES.TASK_ID))
                        .taskKey(r.get(TASKS.TASK_KEY))
                        .projectId(r.get(TASKS.PROJECT_ID))
                        .projectCode(r.get(PROJECTS.PROJECT_CODE))
                        .projectName(r.get(PROJECTS.PROJECT_NAME))
                        .userId(r.get(TASK_ACTIVITIES.USER_ID))
                        .userName(r.get(USERS.FULL_NAME))
                        .action(r.get(TASK_ACTIVITIES.ACTION))
                        .message(r.get(TASK_ACTIVITIES.MESSAGE))
                        .createdTime(r.get(TASK_ACTIVITIES.CREATED_AT))
                        .build());
    }
}
