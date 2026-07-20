package fpt.qn.pms.task.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class InvalidTaskStatusTransitionException extends AppException {

    public InvalidTaskStatusTransitionException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
