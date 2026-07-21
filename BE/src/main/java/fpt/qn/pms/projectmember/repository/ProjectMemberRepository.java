package fpt.qn.pms.projectmember.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.tables.records.ProjectMembersRecord;

public interface ProjectMemberRepository extends Repository<ProjectMembersRecord> {
    Optional<ProjectMembersRecord> findByProjectIdAndUserId(UUID projectId, UUID userId);
}
