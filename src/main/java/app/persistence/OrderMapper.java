package app.persistence;

import app.dto.OrderWithDetailsDTO;
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


    //TODO Fjern orderDate og DeliveryDate og fjern drawingId nullhandling
    public Order createOrder(LocalDateTime orderDate, String status, LocalDateTime deliveryDate, Integer drawingId, int carportId, int customerId) throws DatabaseException
    {
        String sql = "INSERT INTO orders (order_date, status, delivery_date, drawing_id, carport_id, customer_id)" +
                "VALUES (?,?,?,?,?,?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, status);
            ps.setTimestamp(3, Timestamp.valueOf(deliveryDate));
            if(drawingId != null)
            {
                ps.setInt(4, drawingId);
            }
            else
            {
                ps.setNull(4, Types.INTEGER);
            }
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

    public List<OrderWithDetailsDTO> getAllOrdersByStatus(String status) throws DatabaseException {
        String sql = """
            SELECT 
                o.order_id, o.order_date, o.status, o.delivery_date,
                c.carport_id, c.width, c.length, c.height, c.with_shed, 
                c.shed_width, c.shed_length, c.customer_wishes,
                cu.customer_id, cu.firstname, cu.lastname, cu.email, cu.phone, 
                cu.street, cu.house_number, cu.zipcode, cu.city,
                d.drawing_id, d.drawing_data, d.accepted
            FROM orders o
            JOIN carports c ON o.carport_id = c.carport_id
            JOIN customers cu ON o.customer_id = cu.customer_id
            LEFT JOIN drawings d ON o.drawing_id = d.drawing_id
            WHERE o.status = ?
            ORDER BY o.order_date
            """;

        List<OrderWithDetailsDTO> orders = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");

                    Carport carport;
                    boolean withShed = rs.getBoolean("with_shed");
                    if (withShed) {
                        carport = new Carport(
                            rs.getInt("carport_id"),
                            rs.getDouble("width"),
                            rs.getDouble("length"),
                            rs.getDouble("height"),
                            withShed,
                            rs.getDouble("shed_width"),
                            rs.getDouble("shed_length"),
                            rs.getString("customer_wishes")
                        );
                    } else {
                        carport = new Carport(
                            rs.getInt("carport_id"),
                            rs.getDouble("width"),
                            rs.getDouble("length"),
                            rs.getDouble("height"),
                            withShed,
                            rs.getString("customer_wishes")
                        );
                    }

                    Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("street"),
                        rs.getString("house_number"),
                        rs.getInt("zipcode"),
                        rs.getString("city")
                    );

                    Drawing drawing = null;
                    int drawingId = rs.getInt("drawing_id");
                    if (!rs.wasNull()) {
                        drawing = new Drawing(
                                drawingId,
                                rs.getString("drawing_data"));
                    }

                    List<MaterialsLine> materialLines = materialsLinesMapper.getMaterialLinesByOrderId(orderId);

                    orders.add(new OrderWithDetailsDTO(
                        orderId,
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        rs.getString("status"),
                        rs.getTimestamp("delivery_date").toLocalDateTime(),
                        drawing,
                        materialLines,
                        carport,
                        customer
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af ordre: " + e.getMessage());
        }

        return orders;
    }
}

