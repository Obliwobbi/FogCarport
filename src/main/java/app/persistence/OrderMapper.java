package app.persistence;

import app.entities.*;
import app.exceptions.DatabaseException;

import java.time.LocalDateTime;
import java.util.List;

public class OrderMapper
{
    private final ConnectionPool connectionPool;

    public OrderMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Order createOrder(LocalDateTime orderDate, String status, LocalDateTime deliveryDate, Drawing drawing, Carport carport, BillOfMaterials billOfMaterials) throws DatabaseException
    {
        return null;
    }

    public List<Order> getAllOrders() throws DatabaseException
    {
        return null;
    }

    public boolean updateOrderStatus(int orderId, String status) throws DatabaseException
    {
        return true;
    }

    public boolean deleteOrder(int orderId) throws DatabaseException
    {
        return true;
    }
}

