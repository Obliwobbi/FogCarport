package app.persistence;

import app.dto.OrderWithDetailsDTO;
import app.entities.Order;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest
{
    private static final String USER = "postgres";
    private static final String PASSWORD = "ModigsteFryser47";
    private static final String URL = "jdbc:postgresql://164.92.247.68:5432/%s?currentSchema=test";
    private static final String DB = "fogcarport";

    private static ConnectionPool connectionPool;
    private static OrderMapper orderMapper;

    @BeforeAll
    public static void setUpClass()
    {
        try
        {
            connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
            orderMapper = new OrderMapper(connectionPool);

            try (Connection testConnection = connectionPool.getConnection();
                 Statement stmt = testConnection.createStatement())
            {
                stmt.execute("DROP TABLE IF EXISTS test.materials_lines CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.employees CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.orders CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.drawings CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.carports CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.materials CASCADE");
                stmt.execute("DROP TABLE IF EXISTS test.customers CASCADE");

                stmt.execute("""
                            CREATE TABLE test.customers (
                                customer_id SERIAL PRIMARY KEY,
                                firstname VARCHAR(100) NOT NULL,
                                lastname VARCHAR(100) NOT NULL,
                                email VARCHAR(100) NOT NULL,
                                phone VARCHAR(20),
                                street VARCHAR(100),
                                house_number VARCHAR(10),
                                zipcode INT,
                                city VARCHAR(100)
                            )
                        """);

                stmt.execute("""
                        CREATE TABLE test.employees (
                            employee_id SERIAL PRIMARY KEY,
                            name        VARCHAR(100)        NOT NULL,
                            email       VARCHAR(100) UNIQUE NOT NULL,
                            phone       VARCHAR(20)
                        );
                        """);

                stmt.execute("""
                            CREATE TABLE test.materials (
                                id SERIAL PRIMARY KEY,
                                name VARCHAR(100) NOT NULL,
                                description VARCHAR(255),
                                unit INT NOT NULL,
                                unit_type VARCHAR(50) NOT NULL,
                                material_length DECIMAL(10, 2),
                                material_width DECIMAL(10, 2),
                                material_height DECIMAL(10, 2),
                                price DECIMAL(10, 2) NOT NULL
                            )
                        """);

                stmt.execute("""
                            CREATE TABLE test.carports (
                                carport_id SERIAL PRIMARY KEY,
                                width DECIMAL(10, 2) NOT NULL,
                                length DECIMAL(10, 2) NOT NULL,
                                height DECIMAL(10, 2) NOT NULL,
                                with_shed BOOLEAN DEFAULT FALSE,
                                shed_width DECIMAL(10, 2),
                                shed_length DECIMAL(10, 2),
                                customer_wishes VARCHAR(250)
                            )
                        """);

                stmt.execute("""
                            CREATE TABLE test.drawings (
                                drawing_id SERIAL PRIMARY KEY,
                                drawing_data TEXT NOT NULL
                                                        )
                        """);

                stmt.execute("""
                            CREATE TABLE test.orders (
                                order_id SERIAL PRIMARY KEY,
                                order_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                                status VARCHAR(50) NOT NULL DEFAULT 'NY ORDRE',
                                delivery_date TIMESTAMP WITH TIME ZONE DEFAULT (NOW() + INTERVAL '1 year'),
                                drawing_id INT,
                                carport_id INT NOT NULL,
                                customer_id INT NOT NULL,
                                total_price DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
                                employee_id INT,
                                CONSTRAINT fk_drawing FOREIGN KEY (drawing_id) REFERENCES test.drawings(drawing_id) ON DELETE SET NULL,
                                CONSTRAINT fk_carport FOREIGN KEY (carport_id) REFERENCES test.carports(carport_id) ON DELETE CASCADE,
                                CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES test.customers(customer_id) ON DELETE CASCADE,
                                CONSTRAINT fk_employee FOREIGN KEY (employee_id) REFERENCES test.employees(employee_id) ON DELETE CASCADE
                            )
                        """);

                stmt.execute("""
                            CREATE TABLE test.materials_lines (
                                line_id SERIAL PRIMARY KEY,
                                order_id INT NOT NULL,
                                material_id INT,
                                material_name VARCHAR(100) NOT NULL,
                                unit_type VARCHAR(50) NOT NULL,
                                quantity INT NOT NULL,
                                unit_price DECIMAL(10, 2) NOT NULL,
                                line_price DECIMAL(10, 2) NOT NULL,
                                CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES test.orders(order_id) ON DELETE CASCADE,
                                CONSTRAINT fk_material FOREIGN KEY (material_id) REFERENCES test.materials(id) ON DELETE SET NULL
                            )
                        """);
            }
        }
        catch (SQLException e)
        {
            fail("Database connection failed: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp()
    {
        try (Connection connection = connectionPool.getConnection();
             Statement stmt = connection.createStatement())
        {
            stmt.execute("DELETE FROM test.materials_lines");
            stmt.execute("DELETE FROM test.orders");
            stmt.execute("DELETE FROM test.employees");
            stmt.execute("DELETE FROM test.drawings");
            stmt.execute("DELETE FROM test.carports");
            stmt.execute("DELETE FROM test.materials");
            stmt.execute("DELETE FROM test.customers");

            stmt.execute("ALTER SEQUENCE test.employees_employee_id_seq RESTART WITH 1");

            stmt.execute("""
                        INSERT INTO test.employees (name, email, phone)
                        VALUES ('Jesper Person', 'jp@fogcarport.dk','+45 23456789'),
                               ('Toby Person', 'tp@fogcarport.dk','+45 23456790')
                    """);

            stmt.execute("""
                        INSERT INTO test.customers (customer_id, firstname, lastname, email, phone, street, house_number, zipcode, city)
                        VALUES (1, 'Anders', 'Andersen', 'anders@example.com', '+45 12345678', 'Hovedgaden', '10', 2000, 'Frederiksberg'),
                               (2, 'Bente', 'Bentsen', 'bente@example.com', '+45 23456789', 'Vestergade', '25', 8000, 'Aarhus'),
                               (3, 'Christian', 'Christensen', 'christian@example.com', '+45 34567890', 'Østergade', '5', 5000, 'Odense')
                    """);

            stmt.execute("""
                        INSERT INTO test.carports (carport_id, width, length, height, with_shed, shed_width, shed_length, customer_wishes)
                        VALUES (1, 600, 780, 210, FALSE, NULL, NULL, 'Standard carport uden skur'),
                               (2, 600, 600, 210, TRUE, 300, 210, 'Carport med skur til have redskaber'),
                               (3, 780, 600, 240, TRUE, 300, 240, 'Stor carport med skur')
                    """);

            stmt.execute("""
                        INSERT INTO test.drawings (drawing_id, drawing_data)
                        VALUES (1, 'SVG drawing data for standard carport 600x780...'),
                               (2, 'SVG drawing data for carport with shed 600x600...'),
                               (3, 'SVG drawing data for large carport 780x600...')
                    """);

            stmt.execute("""
                        INSERT INTO test.materials (id, name, description, unit, unit_type, material_length, material_width, material_height, price)
                        VALUES (1, '25x200 mm trykimpr. Brædt', 'Understernbrædder', 1, 'stk', 360.00, 20.00, 2.50, 79.00),
                               (2, '25x200 mm trykimpr. Brædt', 'Understernbrædder', 1, 'stk', 540.00, 20.00, 2.50, 118.00),
                               (3, 'Plastmo bundskruer', 'Skruer til tagplader', 200, 'pakke(r)', NULL, NULL, NULL, 129.00)
                    """);

            stmt.execute("SELECT setval('test.customers_customer_id_seq', 4, true)");
            stmt.execute("SELECT setval('test.employees_employee_id_seq', 3, true)");
            stmt.execute("SELECT setval('test.carports_carport_id_seq', 3, true)");
            stmt.execute("SELECT setval('test.drawings_drawing_id_seq', 3, true)");
            stmt.execute("SELECT setval('test.materials_id_seq', 3, true)");


            stmt.execute("""
                        INSERT INTO test.orders (order_id, order_date, status, delivery_date, drawing_id, carport_id, customer_id, total_price, employee_id)
                        VALUES (1, '2024-01-15 10:30:00', 'AFVENTER ACCEPT', '2024-02-15 10:00:00', 1, 1, 1, 0.00, NULL),
                               (2, '2024-01-10 14:20:00', 'BETALT', '2024-02-10 12:00:00', 2, 2, 2, 6484.00, 1),
                               (3, '2024-01-05 09:15:00', 'AFSENDT', '2024-02-01 08:00:00', 3, 3, 3, 8143.00, 2)
                    """);

            stmt.execute("SELECT setval('test.orders_order_id_seq', 3, true)");
        }
        catch (SQLException e)
        {
            fail("Failed to insert test data: " + e.getMessage());
        }
    }

    @Test
    void testConnection() throws SQLException
    {
        assertNotNull(connectionPool.getConnection());
    }

    @Test
    void createOrder() throws DatabaseException
    {
        Order order = orderMapper.createOrder(1, 1, 1);

        assertNotNull(order);
        assertEquals(4, order.getOrderId());
        assertEquals(1, order.getCarportId());
        assertEquals(1, order.getDrawingId());
        assertEquals(1, order.getCustomerId());
    }

    @Test
    void getOrderById() throws DatabaseException
    {
        Order order = orderMapper.getOrderById(1);

        assertNotNull(order);
        assertEquals(1, order.getOrderId());
        assertEquals("AFVENTER ACCEPT", order.getStatus());
        assertEquals(1, order.getCarportId());
        assertEquals(1, order.getCustomerId());
    }

    @Test
    void getAllOrders() throws DatabaseException
    {
        List<Order> orders = orderMapper.getAllOrders();

        assertEquals(3, orders.size());
        assertEquals(1, orders.get(0).getOrderId());
        assertEquals(2, orders.get(1).getOrderId());
        assertEquals(3, orders.get(2).getOrderId());
    }

    @Test
    void updateOrderStatus() throws DatabaseException
    {
        assertTrue(orderMapper.updateOrderStatus(1, "BETALT"));

        Order updatedOrder = orderMapper.getOrderById(1);
        assertEquals("BETALT", updatedOrder.getStatus());
    }

    @Test
    void updateOrderDeliveryDate() throws DatabaseException
    {
        LocalDateTime newDeliveryDate = LocalDateTime.of(2026, 2, 10, 0, 0);
        assertTrue(orderMapper.updateOrderDeliveryDate(3, newDeliveryDate));

        Order updatedOrder = orderMapper.getOrderById(3);
        assertEquals(newDeliveryDate, updatedOrder.getDeliveryDate());
    }

    @Test
    void deleteOrder() throws DatabaseException
    {
        assertTrue(orderMapper.deleteOrder(3));
        assertThrows(DatabaseException.class, () -> orderMapper.getOrderById(3));
    }

    @Test
    void getAllOrdersByStatus() throws DatabaseException
    {
        List<OrderWithDetailsDTO> pendingOrders = orderMapper.getAllOrdersByStatus("AFVENTER ACCEPT");

        assertEquals(1, pendingOrders.size());
        OrderWithDetailsDTO order = pendingOrders.get(0);

        assertEquals(1, order.getOrderId());
        assertEquals("AFVENTER ACCEPT", order.getStatus());

        assertNotNull(order.getCustomer());
        assertEquals("Anders", order.getCustomer().getFirstName());
        assertEquals("Andersen", order.getCustomer().getLastName());

        assertNotNull(order.getCarport());
        assertEquals(600.0, order.getCarport().getWidth());
        assertEquals(780.0, order.getCarport().getLength());
        assertFalse(order.getCarport().isWithShed());

        assertNotNull(order.getDrawing());
        assertEquals(1, order.getDrawing().getDrawingId());
    }

    @Test
    void getAllOrdersByStatusMultipleResults() throws DatabaseException
    {
        orderMapper.createOrder(2, 2, 2);
        orderMapper.updateOrderStatus(4, "BETALT");

        List<OrderWithDetailsDTO> approvedOrders = orderMapper.getAllOrdersByStatus("BETALT");

        assertEquals(2, approvedOrders.size());
    }

    @Test
    void getAllOrdersByStatusNoResults() throws DatabaseException
    {
        List<OrderWithDetailsDTO> orders = orderMapper.getAllOrdersByStatus("IKKE_EKSISTERENDE");

        assertTrue(orders.isEmpty());
    }

    @DisplayName("Update to new Employee")
    @Test
    void updateEmployee() throws DatabaseException
    {
        Order orderBefore =  orderMapper.getOrderById(1);
        orderMapper.updateOrderEmployee(1, 2);
        Order orderAfter = orderMapper.getOrderById(1);
        assertEquals(0, orderBefore.getEmployeeId());
        assertEquals(2, orderAfter.getEmployeeId());
    }

    @DisplayName("Remove Employee from order")
    @Test
    void removeEmployee() throws DatabaseException
    {
        orderMapper.setOrderEmployeeNull(2);
        Order order = orderMapper.getOrderById(2);
        assertEquals(0,order.getEmployeeId());
    }
}