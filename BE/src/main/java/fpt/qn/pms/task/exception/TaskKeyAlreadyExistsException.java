package fpt.qn.pms.task.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class TaskKeyAlreadyExistsException extends AppException {

    public TaskKeyAlreadyExistsException(String taskKey) {
        super(HttpStatus.CONFLICT, "Task key already exists: " + taskKey);
    }
}
