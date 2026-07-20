package fpt.qn.pms.sprint.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSprintRequest {

    @NotNull
    UUID projectId;

    @NotBlank
    @Size(max = 200)
    String sprintName;

    @Size(max = 10000)
    String goal;

    @NotNull
    LocalDate startDate;

    @NotNull
    LocalDate endDate;
}
