package fpt.qn.pms.projectmember.repository;

import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.records.ProjectMembersRecord;

@Repository
public class ProjectMemberRepositoryImpl extends BaseRepository<ProjectMembersRecord> implements ProjectMemberRepository {

    public ProjectMemberRepositoryImpl(DSLContext dsl) {
        super(dsl, PROJECT_MEMBERS);
    }
}
