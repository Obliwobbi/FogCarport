package app.util;

import app.config.ThymeleafConfig;
import app.exceptions.EmailErrorType;
import app.exceptions.EmailException;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

public class EmailSenderHTML
{
    private final String username;
    private final String password;
    private final TemplateEngine templateEngine;

    private static final String VERIFIED_SENDER_EMAIL = System.getenv("MAIL_FROM_ADDRESS");
    private static final String VERIFIED_SENDER_NAME = System.getenv("MAIL_FROM_NAME");

    public EmailSenderHTML()
    {
        // Hent login fra miljøvariabler
        this.username = System.getenv("MAIL_USERNAME");
        this.password = System.getenv("MAIL_PASSWORD");

        if (username == null || password == null)
        {
            throw new IllegalStateException("MAIL_USERNAME og MAIL_PASSWORD miljøvariabler skal være sat.");
        }

        // Genbrug konfiguration fra ThymeleafConfig
        this.templateEngine = ThymeleafConfig.templateEngine();
    }

    public String renderTemplate(String templateName, Map<String, Object> variables)
    {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    public void sendHtmlEmail(String emailRecipient, String subject, String htmlBody) throws EmailException
    {
        try
        {
            Properties props = new Properties();
            props.put("mail.smtp.host", System.getenv("MAIL_SMTP_HOST"));
            props.put("mail.smtp.port", System.getenv("MAIL_SMTP_PORT"));
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.trust", System.getenv("MAIL_SMTP_HOST"));

            //debugging
//            props.put("mail.debug", "true");
//            props.put("mail.smtp.debug", "true");

            Session session = Session.getInstance(props, new Authenticator()
            {
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);


            try
            {
                message.setFrom(new InternetAddress(VERIFIED_SENDER_EMAIL, VERIFIED_SENDER_NAME, "UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new EmailException("Email konfigurationsfejl: encoding problem", EmailErrorType.CONFIGURATION_ERROR, e);
            }

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailRecipient));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=UTF-8");

            Transport.send(message);
        }
        catch (MessagingException e)
        {
            EmailErrorType errorType = parseMessagingException(e);
            throw new EmailException("SMTP send failed for recipient: " + emailRecipient, errorType, e);
        }
    }

    private EmailErrorType parseMessagingException(MessagingException e) {
        String msg = String.valueOf(e.getMessage()).toLowerCase();

        if (msg.contains("authentication") || msg.contains("535")) {
            return EmailErrorType.AUTHENTICATION_FAILED;
        }
        if (msg.contains("550") || msg.contains("invalid")) {
            return EmailErrorType.INVALID_RECIPIENT;
        }
        if (msg.contains("timeout") || msg.contains("connection")) {
            return EmailErrorType.NETWORK_ERROR;
        }
        if (msg.contains("503") || msg.contains("service")) {
            return EmailErrorType.SERVICE_UNAVAILABLE;
        }

        return EmailErrorType.UNKNOWN;
    }
}