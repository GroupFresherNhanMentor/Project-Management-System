package fpt.qn.pms.projectmember.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class ProjectMemberAlreadyActiveException extends AppException {

    public ProjectMemberAlreadyActiveException() {
        super(HttpStatus.CONFLICT, "User is already an active member of this project");
    }
}
