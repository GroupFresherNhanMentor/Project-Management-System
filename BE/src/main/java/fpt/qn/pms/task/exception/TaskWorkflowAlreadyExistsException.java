package fpt.qn.pms.task.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class TaskWorkflowAlreadyExistsException extends AppException {
    public TaskWorkflowAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "This workflow transition already exists");
    }
}
