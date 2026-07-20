package fpt.qn.pms.project.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class ProjectNotFoundException extends AppException {

    public ProjectNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Project not found");
    }
}
