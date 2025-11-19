package app.persistence;

import app.entities.*;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
        String sql = "DELETE FROM orders WHERE order_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                return true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af order med id: " + orderId);
        }
        return false;
    }
}

