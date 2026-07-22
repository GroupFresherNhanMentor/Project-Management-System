package fpt.qn.pms.projectmember.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class LastProjectManagerRemovalForbiddenException extends AppException {

    public LastProjectManagerRemovalForbiddenException() {
        super(HttpStatus.CONFLICT, "A project must have at least one active project manager");
    }
}
