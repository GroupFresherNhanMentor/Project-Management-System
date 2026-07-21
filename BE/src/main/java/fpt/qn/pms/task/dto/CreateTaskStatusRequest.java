package fpt.qn.pms.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTaskStatusRequest {

    @NotBlank
    @Size(max = 50)
    String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color (e.g. #3B4FD9)")
    String color;

    Boolean isInitial;

    Boolean isFinal;
}
