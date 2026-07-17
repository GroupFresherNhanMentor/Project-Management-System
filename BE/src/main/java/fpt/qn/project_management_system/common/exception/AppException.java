package fpt.qn.project_management_system.common.exception;

public class AppException extends RuntimeException {

    private static final long serialVersionUID = -5549322336940535680L;

    public AppException() {
        super("Conflict occurred");
    }

    public AppException(String message) {
        super(message);
    }
}
