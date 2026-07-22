package fpt.qn.pms.projectmember.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class ProjectMemberRemovalForbiddenException extends AppException {

    public ProjectMemberRemovalForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
