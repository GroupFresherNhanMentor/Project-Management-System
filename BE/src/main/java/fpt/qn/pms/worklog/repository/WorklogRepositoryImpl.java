package fpt.qn.pms.worklog.repository;

import static fpt.qn.pms.jooq.Tables.TASKS;
import static fpt.qn.pms.jooq.Tables.USERS;
import static fpt.qn.pms.jooq.Tables.WORKLOGS;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.records.WorklogsRecord;
import fpt.qn.pms.worklog.dto.WorklogDto;
import fpt.qn.pms.worklog.dto.WorklogReportFilterDto;
import fpt.qn.pms.worklog.dto.WorklogReportItem;

@Repository
public class WorklogRepositoryImpl extends BaseRepository<WorklogsRecord> implements WorklogRepository {

    public WorklogRepositoryImpl(DSLContext dsl) {
        super(dsl, WORKLOGS);
    }

    @Override
    public PaginationResult<WorklogDto> findByTaskId(UUID taskId, int page, int size) {
        Condition condition = WORKLOGS.TASK_ID.eq(taskId);
        long total = dsl.fetchCount(WORKLOGS, condition);

        List<WorklogDto> items = dsl.select(
                        WORKLOGS.ID.as("id"),
                        WORKLOGS.TASK_ID.as("taskId"),
                        WORKLOGS.WORK_DATE.as("workDate"),
                        WORKLOGS.HOURS.as("hour"),
                        WORKLOGS.DESCRIPTION.as("description"),
                        USERS.FULL_NAME.as("createdBy")
                )
                .from(WORKLOGS)
                .leftJoin(USERS).on(WORKLOGS.USER_ID.eq(USERS.ID))
                .where(condition)
                .orderBy(WORKLOGS.WORK_DATE.desc(), WORKLOGS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetchInto(WorklogDto.class);

        return new PaginationResult<>(total, items);
    }

    @Override
    public Optional<WorklogDto> findDtoById(UUID id) {
        return dsl.select(
                        WORKLOGS.ID.as("id"),
                        WORKLOGS.TASK_ID.as("taskId"),
                        WORKLOGS.WORK_DATE.as("workDate"),
                        WORKLOGS.HOURS.as("hour"),
                        WORKLOGS.DESCRIPTION.as("description"),
                        USERS.FULL_NAME.as("createdBy")
                )
                .from(WORKLOGS)
                .leftJoin(USERS).on(WORKLOGS.USER_ID.eq(USERS.ID))
                .where(WORKLOGS.ID.eq(id))
                .fetchOptionalInto(WorklogDto.class);
    }

    @Override
    public PaginationResult<WorklogReportItem> getWorklogReport(WorklogReportFilterDto filter) {
        Condition condition = DSL.noCondition();

        if (filter.getProjectId() != null) {
            condition = condition.and(TASKS.PROJECT_ID.eq(filter.getProjectId()));
        }
        if (filter.getUserId() != null) {
            condition = condition.and(WORKLOGS.USER_ID.eq(filter.getUserId()));
        }
        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            condition = condition.and(USERS.USERNAME.likeIgnoreCase("%" + filter.getUsername().trim() + "%"));
        }
        if (filter.getFromDate() != null) {
            condition = condition.and(WORKLOGS.WORK_DATE.greaterOrEqual(filter.getFromDate()));
        }
        if (filter.getToDate() != null) {
            condition = condition.and(WORKLOGS.WORK_DATE.lessOrEqual(filter.getToDate()));
        }

        long total = dsl.selectCount()
                .from(WORKLOGS)
                .join(TASKS).on(WORKLOGS.TASK_ID.eq(TASKS.ID))
                .join(USERS).on(WORKLOGS.USER_ID.eq(USERS.ID))
                .where(condition)
                .fetchOne(0, Long.class);

        int page = filter.getPage();
        int size = filter.getSize();

        List<WorklogReportItem> items = dsl.select(
                        WORKLOGS.ID.as("id"),
                        WORKLOGS.TASK_ID.as("taskId"),
                        TASKS.TASK_KEY.as("taskKey"),
                        TASKS.SUMMARY.as("taskSummary"),
                        WORKLOGS.USER_ID.as("userId"),
                        USERS.USERNAME.as("userName"),
                        WORKLOGS.WORK_DATE.as("workDate"),
                        WORKLOGS.HOURS.as("hour"),
                        WORKLOGS.DESCRIPTION.as("description")
                )
                .from(WORKLOGS)
                .join(TASKS).on(WORKLOGS.TASK_ID.eq(TASKS.ID))
                .join(USERS).on(WORKLOGS.USER_ID.eq(USERS.ID))
                .where(condition)
                .orderBy(WORKLOGS.WORK_DATE.desc(), WORKLOGS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetchInto(WorklogReportItem.class);

        return new PaginationResult<>(total, items);
    }
}
