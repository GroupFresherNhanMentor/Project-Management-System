package fpt.qn.pms.task.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class TaskStatusNameConflictException extends AppException {

    public TaskStatusNameConflictException(String name) {
        super(HttpStatus.CONFLICT, "Task status name already exists in this project: " + name);
    }
}
