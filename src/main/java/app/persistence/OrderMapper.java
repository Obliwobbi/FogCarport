package app.persistence;

import app.entities.*;
import app.exceptions.DatabaseException;

import java.sql.*;
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
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, status);
            ps.setInt(2, orderId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                return true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke opdatere ordre status" + e.getMessage());
        }
        return false;
    }

    public boolean updateOrderDeliveryDate(int orderId, LocalDateTime deliveryDate) throws DatabaseException
    {
        String sql = "UPDATE orders SET delivery_date = ? WHERE order_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setTimestamp(1, Timestamp.valueOf(deliveryDate));
            ps.setInt(2, orderId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                return true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke opdatere ordre LeveringsTidspunkt" + e.getMessage());
        }
        return false;
    }

    public boolean updateOrderBillOfMaterials(int orderId, BillOfMaterials billOfMaterials) throws DatabaseException
    {
        String sql = "UPDATE orders SET bom_id = ? WHERE order_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, billOfMaterials.getBomId());
            ps.setInt(2, orderId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                return true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke opdatere ordre Materialeliste" + e.getMessage());
        }
        return false;
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

