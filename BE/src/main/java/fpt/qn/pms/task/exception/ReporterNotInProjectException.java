package fpt.qn.pms.task.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class ReporterNotInProjectException extends AppException {
    public ReporterNotInProjectException() {
        super(HttpStatus.NOT_FOUND, "Reporter is not a member of the project");
    }
}
