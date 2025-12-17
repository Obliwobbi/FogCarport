package app.services;

import app.dto.OrderWithDetailsDTO;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface EmailService
{
    void sendCarportOffer(OrderWithDetailsDTO orderDetails) throws MessagingException, UnsupportedEncodingException;
}