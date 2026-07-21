package fpt.qn.pms.task.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class InvalidTaskStatusFlagsException extends AppException {

    public InvalidTaskStatusFlagsException() {
        super(HttpStatus.BAD_REQUEST, "A task status cannot be both initial and final");
    }
}
