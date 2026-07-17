package fpt.qn.project_management_system.user.dto;

import fpt.qn.project_management_system.jooq.enums.SysRole;
import jakarta.validation.constraints.Email;
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
public class CreateUserRequest {

    @NotBlank
    @Size(max = 50)
    String employeeId;

    @NotBlank
    @Size(min = 3, max = 50)
    String username;

    @NotBlank
    @Size(min = 8, max = 255)
    String password;

    @NotBlank
    @Size(max = 150)
    String fullName;

    @NotBlank
    @Email
    @Size(max = 150)
    String email;

    @NotNull
    SysRole role;
}
