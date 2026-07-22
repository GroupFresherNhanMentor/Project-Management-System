package fpt.qn.pms.project.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;
import fpt.qn.pms.project.dto.request.CreateProjectRequest;
import fpt.qn.pms.project.dto.request.UpdateProjectRequest;
import fpt.qn.pms.project.dto.response.ProjectDto;

class ProjectMapperTest {

    ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);

    @Test
    void toDto_shouldMapStatusLiteral() {
        ProjectsRecord record = new ProjectsRecord();
        record.setProjectCode("WEB");
        record.setProjectName("Web Project");
        record.setStatus(ProjectStatus.ACTIVE);

        ProjectDto dto = mapper.toDto(record);

        assertThat(dto.getProjectCode()).isEqualTo("WEB");
        assertThat(dto.getProjectName()).isEqualTo("Web Project");
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void updateRecord_shouldKeepProjectCodeImmutable() {
        ProjectsRecord record = new ProjectsRecord();
        record.setProjectCode("WEB");
        record.setProjectName("Old name");
        record.setStartDate(LocalDate.of(2026, 7, 1));
        record.setEndDate(LocalDate.of(2026, 8, 1));
        record.setStatus(ProjectStatus.PLANNING);

        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .projectName("New name")
                .startDate(LocalDate.of(2026, 7, 2))
                .endDate(LocalDate.of(2026, 9, 1))
                .status(ProjectStatus.ACTIVE)
                .build();

        mapper.updateRecord(record, request);

        assertThat(record.getProjectCode()).isEqualTo("WEB");
        assertThat(record.getProjectName()).isEqualTo("New name");
        assertThat(record.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    void toRecord_shouldMapCreateFields() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .projectCode("WEB")
                .projectName("Web Project")
                .description("Description")
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(ProjectStatus.PLANNING)
                .build();

        ProjectsRecord record = mapper.toRecord(request);

        assertThat(record.getProjectCode()).isEqualTo("WEB");
        assertThat(record.getStatus()).isEqualTo(ProjectStatus.PLANNING);
        assertThat(record.getCreatedBy()).isNull();
    }
}
