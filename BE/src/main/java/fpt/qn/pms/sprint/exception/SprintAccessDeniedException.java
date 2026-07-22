package fpt.qn.pms.sprint.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class SprintAccessDeniedException extends AppException {

    public SprintAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "You do not have access to this sprint");
    }
}
