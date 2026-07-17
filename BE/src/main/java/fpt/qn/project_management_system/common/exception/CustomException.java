package fpt.qn.project_management_system.common.exception;

public class CustomException extends RuntimeException {

    private static final long serialVersionUID = -5549322336940535680L;

    public CustomException() {
        super("Conflict occurred");
    }

    public CustomException(String message) {
        super(message);
    }
}
