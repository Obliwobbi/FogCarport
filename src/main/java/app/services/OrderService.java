package app.services;

import app.dto.OrderWithDetailsDTO;
import app.entities.*;
import app.exceptions.DatabaseException;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService
{
    OrderWithDetailsDTO getOrderwithDetails(int orderId) throws DatabaseException;

    List<Order> getAllOrders() throws DatabaseException;

    Order createOrder(LocalDateTime orderDate, String status, LocalDateTime deliveryDate, Integer drawingId, int carportId, int customerId) throws DatabaseException;

    boolean createOrder(int carportId, int customerId) throws DatabaseException;

    boolean deleteOrder(int orderId) throws DatabaseException;

    void updateOrderStatus(int orderId, String status) throws DatabaseException;

    void updateOrderDeliveryDate(int orderId, LocalDateTime deliveryDate) throws DatabaseException;

    void updateCustomerInfo(Customer customer) throws DatabaseException;

    void updateCarport(Carport carport) throws DatabaseException;

    List<Order> getOrdersByStatus(String status) throws DatabaseException;

    List<OrderWithDetailsDTO> getOrdersByStatusDTO(String status) throws DatabaseException;
}

