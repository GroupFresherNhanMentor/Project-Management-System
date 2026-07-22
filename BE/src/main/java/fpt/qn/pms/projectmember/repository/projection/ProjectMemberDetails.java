package fpt.qn.pms.projectmember.repository.projection;

import java.time.OffsetDateTime;
import java.util.UUID;

import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;

public record ProjectMemberDetails(
        UUID id,
        UUID projectId,
        UUID userId,
        String employeeId,
        String userFullName,
        String email,
        ProjectRole projectRole,
        ProjectMemberStatus status,
        OffsetDateTime createdAt) {
}
