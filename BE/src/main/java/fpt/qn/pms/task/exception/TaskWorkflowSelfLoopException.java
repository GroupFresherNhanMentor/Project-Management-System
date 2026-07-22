package fpt.qn.pms.task.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class TaskWorkflowSelfLoopException extends AppException {
    public TaskWorkflowSelfLoopException() {
        super(HttpStatus.BAD_REQUEST, "A status cannot transition to itself");
    }
}
