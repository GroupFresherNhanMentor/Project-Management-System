package fpt.qn.pms.projectmember.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.pms.common.exception.AppException;

public class ProjectMemberHasAssignedTasksException extends AppException {

    public ProjectMemberHasAssignedTasksException() {
        super(HttpStatus.CONFLICT,
                "Cannot remove a member who is assigned to tasks. Transfer or unassign the tasks first");
    }
}
