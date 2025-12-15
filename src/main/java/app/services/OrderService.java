package app.services;

import app.dto.OrderWithDetailsDTO;
import app.entities.*;
import app.exceptions.DatabaseException;
import app.util.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService
{
    OrderWithDetailsDTO getOrderwithDetails(int orderId) throws DatabaseException;

    List<Order> getAllOrders() throws DatabaseException;

    int createOrder(int drawingId, int carportId, int customerId) throws DatabaseException;

    boolean deleteOrder(int orderId) throws DatabaseException;

    void updateOrderStatus(int orderId, Status status) throws DatabaseException;

    List<Employee> getAllEmployees() throws DatabaseException;

    void updateOrderEmployee(int orderId, int employeeId) throws DatabaseException;

    void updateOrderDeliveryDate(int orderId, LocalDateTime deliveryDate) throws DatabaseException;

    void updateCustomerInfo(Customer customer) throws DatabaseException;

    void updateCarport(Carport carport) throws DatabaseException;

    void updateOrderTotalPrice(int orderId) throws DatabaseException;

    List<OrderWithDetailsDTO> getOrdersByStatusDTO(Status status) throws DatabaseException;
}

