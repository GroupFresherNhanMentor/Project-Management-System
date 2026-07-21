package fpt.qn.pms.task.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class TaskStatusNotFoundException extends AppException {

    public TaskStatusNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "Task status not found: " + id);
    }
}
