package fpt.qn.pms.task.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class TaskWorkflowNotFoundException extends AppException {
    public TaskWorkflowNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "Task workflow transition not found: " + id);
    }
}
