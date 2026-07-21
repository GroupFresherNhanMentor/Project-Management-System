package fpt.qn.pms.sprint.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class SprintNotFoundException extends AppException {

    public SprintNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Sprint not found");
    }
}
