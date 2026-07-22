package fpt.qn.pms.projectmember.repository.projection;

import java.time.OffsetDateTime;
import java.util.UUID;

import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.SysRole;

public record ProjectMemberDetails(
        UUID id,
        UUID projectId,
        UUID userId,
        String employeeId,
        String userFullName,
        String email,
        SysRole systemRole,
        ProjectRole projectRole,
        ProjectMemberStatus status,
        OffsetDateTime createdAt) {
}
