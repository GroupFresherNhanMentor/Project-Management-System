package fpt.qn.pms.sprint.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateSprintRequest {

    @Size(max = 200)
    String sprintName;

    @Size(max = 10000)
    String goal;

    LocalDate startDate;

    LocalDate endDate;
}
