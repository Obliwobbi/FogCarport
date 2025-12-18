package app.services;

import app.dto.OrderWithDetailsDTO;
import app.util.EmailSenderHTML;
import app.util.Status;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmailServiceImpl implements EmailService
{
    EmailSenderHTML sender;

    public EmailServiceImpl()
    {
        sender = new EmailSenderHTML();
    }

    @Override
    public void sendCarportOffer(OrderWithDetailsDTO orderDetails) throws MessagingException, UnsupportedEncodingException
    {
        int orderId = orderDetails.getOrderId();
        String email = orderDetails.getCustomer().getEmail();
        String subject;
        boolean hasPaid = Status.PAID.equals(orderDetails.getStatus()) ||
                Status.IN_TRANSIT.equals(orderDetails.getStatus()) ||
                Status.DONE.equals(orderDetails.getStatus());

        if (hasPaid)
        {
            subject = "Ordre bekræftelse på Fog Carport (Ordre #" + orderId + ")";
        }
        else
        {
            subject = "Tilbud på Fog Carport (Ordre #" + orderId + ")";
        }
        Map<String, Object> variables = setVariables(orderDetails);

        String html = sender.renderTemplate("email.html", variables);

        sender.sendHtmlEmail(email, subject, html);
    }

    private Map<String, Object> setVariables(OrderWithDetailsDTO orderDetails)
    {
        Map<String, Object> variables = new HashMap<>();

        String customerName = orderDetails.getCustomer().getFirstName() + " " + orderDetails.getCustomer().getLastName();
        String customerEmail = orderDetails.getCustomer().getEmail();
        String customerPhone = orderDetails.getCustomer().getPhone();
        String customerAddress = orderDetails.getCustomer().getStreet() +
                " " + orderDetails.getCustomer().getHouseNumber() + ", " + orderDetails.getCustomer().getZipcode() + " " + orderDetails.getCustomer().getCity();
        int orderId = orderDetails.getOrderId();

        variables.put("customerName", customerName);
        variables.put("customerEmail", customerEmail);
        variables.put("customerPhone", customerPhone);
        variables.put("customerAddress", customerAddress);

        String orderDate = (orderDetails.getOrderDate()
                .toLocalDate()
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.forLanguageTag("da-DK"))));

        String status = orderDetails.getStatus().getDisplayName();

        variables.put("orderId", orderId);
        variables.put("orderDate", orderDate);
        variables.put("status", status);

        String carportWidth = String.valueOf(orderDetails.getCarport().getWidth());
        String carportLength = String.valueOf(orderDetails.getCarport().getLength());
        String carportHeight = String.valueOf(orderDetails.getCarport().getHeight());
        String shedDimensions = orderDetails.getCarport().getShedWidth() + " cm x " + orderDetails.getCarport().getShedLength() + " cm";

        variables.put("carportWidth", carportWidth);
        variables.put("carportLength", carportLength);
        variables.put("carportHeight", carportHeight);

        boolean hasShed = orderDetails.getCarport().isWithShed();
        variables.put("hasShed", hasShed);

        if (hasShed)
        {
            variables.put("shedDimensions", shedDimensions);
        }

        boolean hasPaid = Status.PAID.equals(orderDetails.getStatus()) ||
                Status.IN_TRANSIT.equals(orderDetails.getStatus()) ||
                Status.DONE.equals(orderDetails.getStatus());

        variables.put("hasPaid", hasPaid);

        String emailText;
        if (hasPaid)
        {
            emailText = "Tak for din Bestilling på en af vores carporte. Nedenfor finder du detaljerne for din Ordre.";
            variables.put("mailText", emailText);

            String deliveryDate = orderDetails.getDeliveryDate()
                    .toLocalDate()
                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            .withLocale(Locale.forLanguageTag("da-DK")));

            variables.put("deliveryDate", deliveryDate);
        }
        else
        {
            emailText = "Tak for din interesse i en af vores carporte. Nedenfor finder du detaljerne for dit tilbud.";
            variables.put("mailText", emailText);
        }

        if (hasPaid && orderDetails.getMaterialsLines() != null)
        {
            variables.put("materialsLines", orderDetails.getMaterialsLines());
        }

        String totalPrice = String.valueOf(orderDetails.getTotalPrice());
        variables.put("totalPrice", totalPrice);

        return variables;
    }

}