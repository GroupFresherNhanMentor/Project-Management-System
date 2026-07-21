package fpt.qn.pms.sprint.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class InvalidSprintStatusTransitionException extends AppException {

    public InvalidSprintStatusTransitionException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
