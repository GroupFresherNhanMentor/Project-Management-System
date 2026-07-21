package fpt.qn.pms.projectmember.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class ProjectMemberNotFoundException extends AppException {

    public ProjectMemberNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Project member not found");
    }
}
