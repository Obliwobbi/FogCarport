package app.exceptions;

public class EmailException extends Exception {
    private final EmailErrorType errorType;

    public EmailException(String message, EmailErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public EmailException(String message, EmailErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public EmailErrorType getErrorType() {
        return errorType;
    }
}