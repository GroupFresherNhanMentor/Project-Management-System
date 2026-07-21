package fpt.qn.pms.project.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class ProjectCodeAlreadyExistsException extends AppException {

    public ProjectCodeAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "Project code already exists");
    }
}
