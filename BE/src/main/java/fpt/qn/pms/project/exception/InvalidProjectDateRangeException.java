package fpt.qn.pms.project.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class InvalidProjectDateRangeException extends AppException {

    public InvalidProjectDateRangeException() {
        super(HttpStatus.BAD_REQUEST, "Project end date must be after or equal to start date");
    }
}
