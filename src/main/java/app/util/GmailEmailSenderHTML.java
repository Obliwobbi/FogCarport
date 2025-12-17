package app.util;

import app.config.ThymeleafConfig;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

public class GmailEmailSenderHTML
{
    private final String username;
    private final String password;
    private final TemplateEngine templateEngine;

    private static final String VERIFIED_SENDER_EMAIL = System.getenv("MAIL_FROM_ADDRESS");
    private static final String VERIFIED_SENDER_NAME = System.getenv("MAIL_FROM_NAME");

    public GmailEmailSenderHTML()
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

    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException
    {
        Properties props = new Properties();
        props.put("mail.smtp.host", System.getenv("MAIL_SMTP_HOST"));
        props.put("mail.smtp.port", System.getenv("MAIL_SMTP_PORT"));
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.trust", System.getenv("MAIL_SMTP_HOST"));
        props.put("mail.debug", "true");
        props.put("mail.smtp.debug", "true");

        Session session = Session.getInstance(props, new Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(VERIFIED_SENDER_EMAIL, VERIFIED_SENDER_NAME, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode sender name", e);
        }

        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(htmlBody, "text/html; charset=UTF-8");

        Transport.send(message);
        System.out.println("HTML-mail sendt til " + to);
    }
}