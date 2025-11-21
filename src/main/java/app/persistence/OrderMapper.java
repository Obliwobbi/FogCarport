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
    private final MaterialsLinesMapper materialsLinesMapper;

    public OrderMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
        this.materialsLinesMapper = new MaterialsLinesMapper(connectionPool);
    }

    public Order createOrder(LocalDateTime orderDate, String status, LocalDateTime deliveryDate, Integer drawingId, int carportId, int customerId) throws DatabaseException
    {
        String sql = "INSERT INTO orders (order_date, status, delivery_date, drawing_id, carport_id, customer_id)" +
                "VALUES (?,?,?,?,?,?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setTimestamp(1, Timestamp.valueOf(orderDate));
            ps.setString(2, status);
            ps.setTimestamp(3, Timestamp.valueOf(deliveryDate));
            ps.setInt(4, drawingId);
            ps.setInt(5, carportId);
            ps.setInt(6, customerId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected != 1)
            {
                throw new DatabaseException("Uventet fejl");
            }

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next())
            {
                int orderId = rs.getInt(1);
                List<MaterialsLine> lines = new ArrayList<>();

                Order order = new Order(orderId, orderDate, status, deliveryDate, drawingId, carportId, lines, customerId);

                lines = order.getMaterialLines();

                if (lines != null)
                {
                    for (MaterialsLine line : lines)
                    {
                        materialsLinesMapper.createMaterialLine(orderId, line);
                    }
                }

                return order;
            }

        }
        catch (SQLException e)
            {
                throw new DatabaseException("Databasefejl ved oprettelse af ordre " + e.getMessage());
            }
        return null;
    }

    public Order getOrderById(int orderId) throws DatabaseException
    {
        String sql = "SELECT * FROM orders WHERE order_id= ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    List<MaterialsLine> materialsLines = materialsLinesMapper.getMaterialLinesByOrderId(orderId);
                    return new Order(orderId,
                            rs.getTimestamp("order_date").toLocalDateTime(),
                            rs.getString("status"),
                            rs.getTimestamp("delivery_date").toLocalDateTime(),
                            rs.getInt("drawing_id"),
                            rs.getInt("carport_id"),
                            materialsLines,
                            rs.getInt("customer_id"));
                }
            }
            throw new DatabaseException("Der blev ikke fundet en ordre med id: " + orderId);
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af data for order" + e.getMessage());
        }
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
                    int orderId = rs.getInt("order_id");
                    List<MaterialsLine> materialsLines = materialsLinesMapper.getMaterialLinesByOrderId(orderId);

                    Order order = new Order(
                            orderId,
                            rs.getTimestamp("order_date").toLocalDateTime(),
                            rs.getString("status"),
                            rs.getTimestamp("delivery_date").toLocalDateTime(),
                            rs.getInt("drawing_id"),
                            rs.getInt("carport_id"),
                            materialsLines,
                            rs.getInt("customer_id"));

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

