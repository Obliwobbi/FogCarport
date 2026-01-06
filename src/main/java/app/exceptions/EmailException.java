package app.exceptions;

public class EmailException extends Exception {
    private final EmailErrorType errorType;

    public enum EmailErrorType {
        CONFIGURATION_ERROR,    //forkert SMTP setup
        AUTHENTICATION_FAILED,  //forkert password
        INVALID_RECIPIENT,      //ugyldig email
        NETWORK_ERROR,          //netværk/timeout
        SERVICE_UNAVAILABLE,    //sendGrid nede
        UNKNOWN
    }

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