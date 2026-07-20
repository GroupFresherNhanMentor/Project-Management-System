package fpt.qn.pms.task.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class AssigneeNotInProjectException extends AppException {

    public AssigneeNotInProjectException() {
        super(HttpStatus.BAD_REQUEST, "Assignee must be a member of the project");
    }
}
