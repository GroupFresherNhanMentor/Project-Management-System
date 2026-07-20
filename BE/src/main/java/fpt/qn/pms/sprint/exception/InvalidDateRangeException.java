package fpt.qn.pms.sprint.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class InvalidDateRangeException extends AppException {

    public InvalidDateRangeException() {
        super(HttpStatus.BAD_REQUEST, "end_date must be after or equal to start_date");
    }
}
