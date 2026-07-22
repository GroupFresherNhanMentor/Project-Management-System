package fpt.qn.pms.project.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class ProjectAccessDeniedException extends AppException {

    public ProjectAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "You do not have access to this project");
    }
}
