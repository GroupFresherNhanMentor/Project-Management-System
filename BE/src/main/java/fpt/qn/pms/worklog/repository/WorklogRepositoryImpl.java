package fpt.qn.pms.worklog.repository;

import static fpt.qn.pms.jooq.Tables.WORKLOGS;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.records.WorklogsRecord;

@Repository
public class WorklogRepositoryImpl extends BaseRepository<WorklogsRecord> implements WorklogRepository {

    public WorklogRepositoryImpl(DSLContext dsl) {
        super(dsl, WORKLOGS);
    }
}
