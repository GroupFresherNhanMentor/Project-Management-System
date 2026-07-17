package fpt.qn.project_management_system.projectmember.repository;

import static fpt.qn.project_management_system.jooq.Tables.PROJECT_MEMBERS;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.project_management_system.common.repository.BaseRepository;
import fpt.qn.project_management_system.jooq.tables.records.ProjectMembersRecord;

@Repository
public class ProjectMemberRepositoryImpl extends BaseRepository<ProjectMembersRecord> implements ProjectMemberRepository {

    public ProjectMemberRepositoryImpl(DSLContext dsl) {
        super(dsl, PROJECT_MEMBERS);
    }
}
