package fpt.qn.pms.projectmember.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class ProjectMemberUserInactiveException extends AppException {

    public ProjectMemberUserInactiveException() {
        super(HttpStatus.CONFLICT, "Only active users can be added to a project");
    }
}
