package fpt.qn.project_management_system.task.repository;

import static fpt.qn.project_management_system.jooq.Tables.TASKS;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.project_management_system.common.repository.BaseRepository;
import fpt.qn.project_management_system.jooq.tables.records.TasksRecord;

@Repository
public class TaskRepositoryImpl extends BaseRepository<TasksRecord> implements TaskRepository {

    public TaskRepositoryImpl(DSLContext dsl) {
        super(dsl, TASKS);
    }
}
