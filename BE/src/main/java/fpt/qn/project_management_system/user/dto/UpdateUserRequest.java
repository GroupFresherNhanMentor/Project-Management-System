package fpt.qn.project_management_system.user.dto;

import fpt.qn.project_management_system.jooq.enums.SysRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserRequest {

    @Size(max = 150)
    String fullName;

    @Email
    @Size(max = 150)
    String email;

    SysRole role;
}
