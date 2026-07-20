package fpt.qn.pms.project.repository;

import static fpt.qn.pms.jooq.Tables.PROJECTS;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;

@Repository
public class ProjectRepositoryImpl extends BaseRepository<ProjectsRecord> implements ProjectRepository {

    public ProjectRepositoryImpl(DSLContext dsl) {
        super(dsl, PROJECTS);
    }
}
