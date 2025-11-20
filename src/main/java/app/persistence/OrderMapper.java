package app.persistence;

import app.entities.*;
import app.exceptions.DatabaseException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper
{
    private final ConnectionPool connectionPool;

    public OrderMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Order createOrder(LocalDateTime orderDate, String status, LocalDateTime deliveryDate, Integer drawingId, int carportId, Integer billOfMaterialsId, int customerId) throws DatabaseException
    {
        String sql = "INSERT INTO orders (order_date, status, delivery_date, drawing_id, carport_id, bom_id, customer_id)" +
                "VALUES (?,?,?,?,?,?,?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setTimestamp(1, Timestamp.valueOf(orderDate));
            ps.setString(2, status);
            ps.setTimestamp(3, Timestamp.valueOf(deliveryDate));
            ps.setInt(4, drawingId);
            ps.setInt(5, carportId);

            if (billOfMaterialsId != null)
            {
                ps.setInt(6, billOfMaterialsId);
            }
            else
            {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setInt(7, customerId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected != 1)
            {
                throw new DatabaseException("Uventet fejl");
            }

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next())
            {
                int orderId = rs.getInt(1);
                return new Order(orderId, orderDate, status, deliveryDate, drawingId, carportId, billOfMaterialsId, customerId);
            }
        }
        catch (SQLException e)
        {
            if (e.getSQLState().equals("23505")) // error code is the standard for catching unique constraint errors in PostgresSQL
            {
                throw new DatabaseException("ordre findes allerede");
            }
            else
            {
                throw new DatabaseException("Databasefejl ved oprettelse af ordre " + e.getMessage());
            }
        }
        return null;
    }

    public List<Order> getAllOrders() throws DatabaseException
    {
        String sql = "SELECT * FROM orders";

        List<Order> orders = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    Integer bomId = (Integer) rs.getObject(7); //return null, if column is null

                    Order order = new Order(rs.getInt(1),
                            rs.getTimestamp(2).toLocalDateTime(),
                            rs.getString(3),
                            rs.getTimestamp(4).toLocalDateTime(),
                            rs.getInt(5),
                            rs.getInt(6),
                            bomId,
                            rs.getInt(8));

                    orders.add(order);
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke hente ordrer fra databasen " + e.getMessage());
        }
        return orders;
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

