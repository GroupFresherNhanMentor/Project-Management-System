package fpt.qn.pms.task.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class FinalStatusCannotHaveOutgoingTransitionException extends AppException {
    public FinalStatusCannotHaveOutgoingTransitionException() {
        super(HttpStatus.BAD_REQUEST, "A final status cannot have outgoing transitions");
    }
}
