package fpt.qn.pms.projectmember.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class ProjectMemberAccessDeniedException extends AppException {

    public ProjectMemberAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "You do not have access to project members");
    }
}
