package fpt.qn.pms.user.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class EmployeeIdAlreadyExistsException extends AppException {

    public EmployeeIdAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "Employee ID already exists");
    }
}
