package fpt.qn.pms.sprint.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class ActiveSprintAlreadyExistsException extends AppException {

    public ActiveSprintAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "Project already has an active sprint");
    }
}
