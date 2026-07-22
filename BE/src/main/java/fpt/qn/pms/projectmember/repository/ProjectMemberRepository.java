package fpt.qn.pms.projectmember.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.tables.records.ProjectMembersRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.projectmember.repository.projection.ProjectMemberDetails;

public interface ProjectMemberRepository extends Repository<ProjectMembersRecord> {
    Optional<ProjectMembersRecord> findByProjectIdAndUserId(UUID projectId, UUID userId);

    Optional<ProjectMembersRecord> findByIdAndProjectId(UUID id, UUID projectId);

    Optional<ProjectMemberDetails> findDetailsByIdAndProjectId(UUID id, UUID projectId);

    Optional<ProjectMemberDetails> findDetailsByProjectIdAndUserId(UUID projectId, UUID userId);

    PaginationResult<ProjectMemberDetails> findAllByProjectId(
            UUID projectId, String keyword, int page, int size);

    PaginationResult<UsersRecord> findAvailableUsers(
            UUID projectId, String keyword, int page, int size);

    boolean existsActiveByProjectIdAndUserId(UUID projectId, UUID userId);

    boolean existsActiveByProjectIdAndUserIdAndRole(UUID projectId, UUID userId, ProjectRole role);

    boolean existsActiveByUserIdAndRole(UUID userId, ProjectRole role);

    long countActiveByProjectIdAndRole(UUID projectId, ProjectRole role);
}
