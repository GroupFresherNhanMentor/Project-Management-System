package fpt.qn.pms.task.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class TaskNotFoundException extends AppException {

    public TaskNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Task not found");
    }
}
