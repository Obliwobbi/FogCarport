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
        String subject = "Tilbud på Fog Carport (Ordre #" + orderId + ")";

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


        String orderDate = String.valueOf(orderDetails.getOrderDate());
        String status = orderDetails.getStatus();
//        String deliveryDate = String.valueOf(orderDetails.getDeliveryDate());

        variables.put("orderId", orderId);
        variables.put("orderDate", orderDate);
//        variables.put("deliveryDate", deliveryDate);
        variables.put("status", status);

        String carportWidth = String.valueOf(orderDetails.getCarport().getWidth());
        String carportLength = String.valueOf(orderDetails.getCarport().getLength());
        String carportHeight = String.valueOf(orderDetails.getCarport().getHeight());
        String shedDimensions = orderDetails.getCarport().getShedWidth() + " x " + orderDetails.getCarport().getShedLength();

        variables.put("carportWidth", carportWidth);
        variables.put("carportLength", carportLength);
        variables.put("carportHeight", carportHeight);

        boolean hasShed = orderDetails.getCarport().isWithShed();
        variables.put("hasShed", hasShed);

        if(hasShed)
        {
            variables.put("shedDimensions", shedDimensions);
        }

        String totalPrice = String.valueOf(orderDetails.getTotalPrice());
        variables.put("totalPrice", totalPrice);



        return variables;
    }

}