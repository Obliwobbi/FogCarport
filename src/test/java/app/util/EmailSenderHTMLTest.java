package app.util;

import app.exceptions.EmailErrorType;
import app.exceptions.EmailException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailSenderHTMLTest
{

    @Test
    void sendHtmlEmail()
    {
    }

    @Test
    void testInvalidRecipientEmail()
    {
        EmailException e = new EmailException("Invalid recipient", EmailErrorType.INVALID_RECIPIENT);

        assertEquals("Kundens email-adresse er ugyldig - tjek info, eller kontakt telefonisk", e.getErrorType().getUserMessage());
    }
}