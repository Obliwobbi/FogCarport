package app.services;

import app.dto.OrderWithDetailsDTO;
import app.exceptions.EmailException;

public interface EmailService
{
    void sendCarportOffer(OrderWithDetailsDTO orderDetails) throws EmailException;
}