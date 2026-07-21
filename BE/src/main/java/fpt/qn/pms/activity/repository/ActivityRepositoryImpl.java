package fpt.qn.pms.activity.repository;

import static fpt.qn.pms.jooq.Tables.TASK_ACTIVITIES;
import static fpt.qn.pms.jooq.Tables.USERS;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.activity.dto.TaskActivityDto;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.BaseRepository;
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
                    USERS.USERNAME,
                    TASK_ACTIVITIES.ACTION,
                    TASK_ACTIVITIES.OLD_VALUE,
                    TASK_ACTIVITIES.NEW_VALUE,
                    TASK_ACTIVITIES.CREATED_AT
                )
                .from(TASK_ACTIVITIES)
                .leftJoin(USERS).on(TASK_ACTIVITIES.USER_ID.eq(USERS.ID))
                .where(TASK_ACTIVITIES.TASK_ID.eq(taskId))
                .orderBy(TASK_ACTIVITIES.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch(r -> TaskActivityDto.builder()
                        .id(r.get(TASK_ACTIVITIES.ID))
                        .taskId(r.get(TASK_ACTIVITIES.TASK_ID))
                        .userId(r.get(TASK_ACTIVITIES.USER_ID))
                        .userName(r.get(USERS.USERNAME))
                        .action(r.get(TASK_ACTIVITIES.ACTION))
                        .oldValue(r.get(TASK_ACTIVITIES.OLD_VALUE))
                        .newValue(r.get(TASK_ACTIVITIES.NEW_VALUE))
                        .createdTime(r.get(TASK_ACTIVITIES.CREATED_AT))
                        .build()
                );

        return new PaginationResult<>(total, items);
    }
}
