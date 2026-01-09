package app.exceptions;

public enum EmailErrorType {
    AUTHENTICATION_FAILED("Email konfigurationsfejl - kontakt IT"),
    INVALID_RECIPIENT("Kundens email-adresse er ugyldig - tjek info, eller kontakt telefonisk"),
    NETWORK_ERROR("Netværksfejl - prøv igen"),
    SERVICE_UNAVAILABLE("Email service midlertidigt nede - prøv igen senere"),
    CONFIGURATION_ERROR("System konfigurationsfejl - kontakt IT support"),
    UNKNOWN("Kunne ikke sende email - kontakt IT");

    private final String userMessage;

    EmailErrorType(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
