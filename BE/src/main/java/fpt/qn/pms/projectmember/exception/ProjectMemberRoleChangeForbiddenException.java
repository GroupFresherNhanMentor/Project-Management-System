package fpt.qn.pms.projectmember.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class ProjectMemberRoleChangeForbiddenException extends AppException {

    public ProjectMemberRoleChangeForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
