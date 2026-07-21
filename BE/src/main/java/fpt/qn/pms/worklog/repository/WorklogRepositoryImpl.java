package fpt.qn.pms.worklog.repository;

import static fpt.qn.pms.jooq.Tables.TASKS;
import static fpt.qn.pms.jooq.Tables.USERS;
import static fpt.qn.pms.jooq.Tables.WORKLOGS;

import java.util.List;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.records.WorklogsRecord;
import fpt.qn.pms.worklog.dto.WorklogReportFilterDto;
import fpt.qn.pms.worklog.dto.WorklogReportItem;

@Repository
public class WorklogRepositoryImpl extends BaseRepository<WorklogsRecord> implements WorklogRepository {

    public WorklogRepositoryImpl(DSLContext dsl) {
        super(dsl, WORKLOGS);
    }

    @Override
    public PaginationResult<WorklogsRecord> findByTaskId(UUID taskId, int page, int size) {
        Condition condition = WORKLOGS.TASK_ID.eq(taskId);
        long total = dsl.fetchCount(WORKLOGS, condition);

        List<WorklogsRecord> items = dsl.selectFrom(WORKLOGS)
                .where(condition)
                .orderBy(WORKLOGS.WORK_DATE.desc(), WORKLOGS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch();

        return new PaginationResult<>(total, items);
    }

    @Override
    public PaginationResult<WorklogReportItem> getWorklogReport(WorklogReportFilterDto filter) {
        Condition condition = DSL.noCondition();

        if (filter.getProject() != null) {
            condition = condition.and(TASKS.PROJECT_ID.eq(filter.getProject()));
        }
        if (filter.getUser() != null) {
            condition = condition.and(WORKLOGS.USER_ID.eq(filter.getUser()));
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
                        WORKLOGS.ID,
                        WORKLOGS.TASK_ID,
                        TASKS.TASK_KEY,
                        TASKS.SUMMARY.as("taskSummary"),
                        WORKLOGS.USER_ID,
                        USERS.FULL_NAME.as("userName"),
                        WORKLOGS.WORK_DATE,
                        WORKLOGS.HOURS.as("hour"),
                        WORKLOGS.DESCRIPTION
                )
                .from(WORKLOGS)
                .join(TASKS).on(WORKLOGS.TASK_ID.eq(TASKS.ID))
                .join(USERS).on(WORKLOGS.USER_ID.eq(USERS.ID))
                .where(condition)
                .orderBy(WORKLOGS.WORK_DATE.desc(), WORKLOGS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch(record -> WorklogReportItem.builder()
                        .id(record.get(WORKLOGS.ID))
                        .taskId(record.get(WORKLOGS.TASK_ID))
                        .taskKey(record.get(TASKS.TASK_KEY))
                        .taskSummary(record.get(TASKS.SUMMARY.as("taskSummary")))
                        .userId(record.get(WORKLOGS.USER_ID))
                        .userName(record.get(USERS.FULL_NAME.as("userName")))
                        .workDate(record.get(WORKLOGS.WORK_DATE))
                        .hour(record.get(WORKLOGS.HOURS.as("hour")))
                        .description(record.get(WORKLOGS.DESCRIPTION))
                        .build());

        return new PaginationResult<>(total, items);
    }
}
