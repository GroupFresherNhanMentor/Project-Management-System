package fpt.qn.project_management_system.worklog.repository;

import static fpt.qn.project_management_system.jooq.Tables.WORKLOGS;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.project_management_system.common.repository.BaseRepository;
import fpt.qn.project_management_system.jooq.tables.records.WorklogsRecord;

@Repository
public class WorklogRepositoryImpl extends BaseRepository<WorklogsRecord> implements WorklogRepository {

    public WorklogRepositoryImpl(DSLContext dsl) {
        super(dsl, WORKLOGS);
    }
}
