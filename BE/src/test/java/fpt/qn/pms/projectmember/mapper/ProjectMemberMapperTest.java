package fpt.qn.pms.projectmember.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.tables.records.ProjectMembersRecord;
import fpt.qn.pms.projectmember.dto.request.AddProjectMemberRequest;
import fpt.qn.pms.projectmember.dto.response.ProjectMemberDto;
import fpt.qn.pms.projectmember.repository.projection.ProjectMemberDetails;

class ProjectMemberMapperTest {

    ProjectMemberMapper mapper = Mappers.getMapper(ProjectMemberMapper.class);

    @Test
    void toDto_shouldMapProjectionAndEnumLiterals() {
        ProjectMemberDetails details = new ProjectMemberDetails(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "EMP-001", "Developer One", "dev1@test.com",
                ProjectRole.DEV, ProjectMemberStatus.ACTIVE, OffsetDateTime.now());

        ProjectMemberDto dto = mapper.toDto(details);

        assertThat(dto.getId()).isEqualTo(details.id());
        assertThat(dto.getUserFullName()).isEqualTo("Developer One");
        assertThat(dto.getProjectRole()).isEqualTo("DEV");
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void toRecord_shouldMapProjectAndRequestFields() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AddProjectMemberRequest request = AddProjectMemberRequest.builder()
                .userId(userId)
                .projectRole(ProjectRole.TESTER)
                .build();

        ProjectMembersRecord record = mapper.toRecord(projectId, request);

        assertThat(record.getProjectId()).isEqualTo(projectId);
        assertThat(record.getUserId()).isEqualTo(userId);
        assertThat(record.getProjectRole()).isEqualTo(ProjectRole.TESTER);
        assertThat(record.getStatus()).isNull();
    }
}
