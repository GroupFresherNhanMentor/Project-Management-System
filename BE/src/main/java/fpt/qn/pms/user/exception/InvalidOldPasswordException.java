package fpt.qn.pms.user.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class InvalidOldPasswordException extends AppException {

    public InvalidOldPasswordException() {
        super(HttpStatus.BAD_REQUEST, "Current password is incorrect");
    }
}
