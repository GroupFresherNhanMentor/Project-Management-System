package fpt.qn.pms.task.repository;

import static fpt.qn.pms.jooq.Tables.TASKS;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.records.TasksRecord;

@Repository
public class TaskRepositoryImpl extends BaseRepository<TasksRecord> implements TaskRepository {

    public TaskRepositoryImpl(DSLContext dsl) {
        super(dsl, TASKS);
    }
}
