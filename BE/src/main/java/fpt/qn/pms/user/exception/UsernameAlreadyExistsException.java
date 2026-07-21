package fpt.qn.pms.user.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class UsernameAlreadyExistsException extends AppException {

    public UsernameAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "Username already exists");
    }
}
