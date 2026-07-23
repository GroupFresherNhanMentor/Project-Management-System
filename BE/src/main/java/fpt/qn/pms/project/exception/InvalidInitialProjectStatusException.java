package fpt.qn.pms.project.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class InvalidInitialProjectStatusException extends AppException {

    public InvalidInitialProjectStatusException() {
        super(HttpStatus.BAD_REQUEST, "A new project cannot have COMPLETED status");
    }
}
