package app.services;

import app.dto.OrderWithDetailsDTO;
import app.util.GmailEmailSenderHTML;
import jakarta.mail.MessagingException;

import java.util.HashMap;
import java.util.Map;

public class EmailServiceImpl implements EmailService
{
    private EmailService emailService;
    GmailEmailSenderHTML sender;

    public EmailServiceImpl()
    {
        sender = new GmailEmailSenderHTML();
    }

    @Override
    public void sendCarportOffer(OrderWithDetailsDTO orderDetails) throws MessagingException
    {
        int orderId = orderDetails.getOrderId();
        String email = orderDetails.getCustomer().getEmail();
        String subject;

        if ("BETALT".equals(orderDetails.getStatus()) || "AFSENDT".equals(orderDetails.getStatus()) ||
                "AFSLUTTET".equals(orderDetails.getStatus()))
        {
            subject = "OrdreBekræftigelse på Fog Carport (Ordre #" + orderId + ")";
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


        String orderDate = String.valueOf(orderDetails.getOrderDate().toLocalDate());
        String status = orderDetails.getStatus();
        String deliveryDate = String.valueOf(orderDetails.getDeliveryDate().toLocalDate());

        variables.put("orderId", orderId);
        variables.put("orderDate", orderDate);
        variables.put("deliveryDate", deliveryDate);
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

        boolean hasPaid = "BETALT".equals(orderDetails.getStatus()) ||
                "AFSENDT".equals(orderDetails.getStatus()) ||
                "AFSLUTTET".equals(orderDetails.getStatus());

        variables.put("hasPaid", hasPaid);

        String emailText;
        if(hasPaid)
        {
            emailText = "Tak for din Bestilling på en af vores carporte. Nedenfor finder du detaljerne for din Ordre.";
            variables.put("mailText", emailText);
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