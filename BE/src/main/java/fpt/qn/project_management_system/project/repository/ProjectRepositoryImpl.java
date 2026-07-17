package fpt.qn.project_management_system.project.repository;

import static fpt.qn.project_management_system.jooq.Tables.PROJECTS;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.project_management_system.common.repository.BaseRepository;
import fpt.qn.project_management_system.jooq.tables.records.ProjectsRecord;

@Repository
public class ProjectRepositoryImpl extends BaseRepository<ProjectsRecord> implements ProjectRepository {

    public ProjectRepositoryImpl(DSLContext dsl) {
        super(dsl, PROJECTS);
    }
}
