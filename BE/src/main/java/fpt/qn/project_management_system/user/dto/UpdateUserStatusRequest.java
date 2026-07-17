package fpt.qn.project_management_system.user.dto;

import fpt.qn.project_management_system.jooq.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserStatusRequest {

    @NotNull
    UserStatus status;
}
