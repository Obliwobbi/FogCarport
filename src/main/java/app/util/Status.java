package app.util;

public enum Status
{
    NEW("Ny Ordre"),
    PENDING("Afventer Accept"),
    PAID("Betalt"),
    IN_TRANSIT("Afsendt"),
    DONE("Afsluttet");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public static Status fromDisplayName(String displayName) {
        for (Status status : values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + displayName);
    }
}
