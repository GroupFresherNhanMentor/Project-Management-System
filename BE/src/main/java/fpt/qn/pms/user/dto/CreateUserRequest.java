package fpt.qn.pms.user.dto;

import fpt.qn.pms.jooq.enums.SysRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
