package fpt.qn.project_management_system.activity.repository;

import static fpt.qn.project_management_system.jooq.Tables.TASK_ACTIVITIES;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.project_management_system.common.repository.BaseRepository;
import fpt.qn.project_management_system.jooq.tables.records.TaskActivitiesRecord;

@Repository
public class ActivityRepositoryImpl extends BaseRepository<TaskActivitiesRecord> implements ActivityRepository {

    public ActivityRepositoryImpl(DSLContext dsl) {
        super(dsl, TASK_ACTIVITIES);
    }
}
