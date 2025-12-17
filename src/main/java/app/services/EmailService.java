package app.services;

import app.dto.OrderWithDetailsDTO;
import jakarta.mail.MessagingException;

public interface EmailService
{
    void sendCarportOffer(OrderWithDetailsDTO orderDetails) throws MessagingException;
}