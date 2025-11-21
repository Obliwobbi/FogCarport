package app.persistence;

import app.entities.Order;
import app.exceptions.DatabaseException;
import com.sun.source.tree.AssertTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest
{
    private final static String USER = "postgres";
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
            connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, "fogcarport");
            orderMapper = new OrderMapper(connectionPool);

            try (Connection testConnection = connectionPool.getConnection())
            {
                try (Statement stmt = testConnection.createStatement())
                {
                    stmt.execute("DROP TABLE IF EXISTS test.materials_lines CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.orders CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.bills_of_materials CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.drawings CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.carports CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.materials CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.customers CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.employees CASCADE");

                    // Create Customers Table
                    stmt.execute("""
                               CREATE TABLE test.customers (
                                     customer_id SERIAL PRIMARY KEY,
                                     firstname VARCHAR(100) NOT NULL,
                                     lastname VARCHAR(100) NOT NULL,
                                     email VARCHAR(100) UNIQUE NOT NULL,
                                     phone VARCHAR(20),
                                     street VARCHAR(100),
                                     house_number VARCHAR(10),
                                     zipcode INT,
                                     city VARCHAR(100)
                                 )
                            """);

                    // Create Carports Table
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

                    // Create Drawings Table
                    stmt.execute("""
                               CREATE TABLE test.drawings (
                                     drawing_id SERIAL PRIMARY KEY,
                                     drawing_data TEXT NOT NULL,
                                     accepted BOOLEAN DEFAULT FALSE
                                 )
                            """);


                    // Create Orders Table
                    stmt.execute("""
                               CREATE TABLE test.orders (
                                     order_id SERIAL PRIMARY KEY,
                                     order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     status VARCHAR(50) NOT NULL DEFAULT 'AFVENTER ACCEPT',
                                     delivery_date TIMESTAMP,
                                     drawing_id INT,
                                     carport_id INT NOT NULL,
                                                                         customer_id INT NOT NULL,
                                     CONSTRAINT fk_drawing FOREIGN KEY (drawing_id) REFERENCES test.drawings(drawing_id) ON DELETE SET NULL,
                                     CONSTRAINT fk_carport FOREIGN KEY (carport_id) REFERENCES test.carports(carport_id) ON DELETE CASCADE,
                                     CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES test.customers(customer_id) ON DELETE CASCADE
                                 )
                            """);

                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
                fail("Database connection failed: " + e.getMessage());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Failed to set up test database");
        }
    }


    @BeforeEach
    void setUp()
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {
                // Delete in reverse order of dependencies
                stmt.execute("DELETE FROM test.orders");
                stmt.execute("DELETE FROM test.drawings");
                stmt.execute("DELETE FROM test.carports");
                stmt.execute("DELETE FROM test.customers");

                // Insert test customers
                stmt.execute("""
                            INSERT INTO test.customers (customer_id, firstname, lastname, email, phone, street, house_number, zipcode, city)
                            VALUES (1, 'Anders', 'Andersen', 'anders@example.com', '+45 12345678', 'Hovedgaden', '10', 2000, 'Frederiksberg'),
                                   (2, 'Bente', 'Bentsen', 'bente@example.com', '+45 23456789', 'Vestergade', '25', 8000, 'Aarhus'),
                                   (3, 'Christian', 'Christensen', 'christian@example.com', '+45 34567890', 'Østergade', '5', 5000, 'Odense')
                        """);

                // Insert test carports
                stmt.execute("""
                            INSERT INTO test.carports (carport_id, width, length, height, with_shed, shed_width, shed_length, customer_wishes)
                            VALUES (1, 600, 780, 210, FALSE, NULL, NULL, 'Standard carport uden skur'),
                                   (2, 600, 600, 210, TRUE, 300, 210, 'Carport med skur til haven redskaber'),
                                   (3, 780, 600, 240, TRUE, 300, 240, 'Stor carport med skur')
                        """);

                // Insert test drawings
                stmt.execute("""
                            INSERT INTO test.drawings (drawing_id, drawing_data, accepted)
                            VALUES (1, 'SVG drawing data for carport 1...', FALSE),
                                   (2, 'SVG drawing data for carport 2...', TRUE),
                                   (3, 'SVG drawing data for carport 3...', TRUE)
                        """);


                // Reset sequences
                stmt.execute("SELECT setval('test.customers_customer_id_seq', 3, true)");
                stmt.execute("SELECT setval('test.carports_carport_id_seq', 3, true)");
                stmt.execute("SELECT setval('test.drawings_drawing_id_seq', 3, true)");

                // Insert test orders
                stmt.execute("""
                            INSERT INTO test.orders (order_id, order_date, status, delivery_date, drawing_id, carport_id, customer_id)
                            VALUES (1, '2024-01-15 10:30:00', 'AFVENTER ACCEPT', '2024-02-15 10:00:00', 1, 1, 1);
                        
                        """);

                stmt.execute("""
                            INSERT INTO test.orders (order_id, order_date, status, delivery_date, drawing_id, carport_id, customer_id)
                            VALUES (2, '2024-01-10 14:20:00', 'GODKENDT', '2024-02-10 12:00:00', 2, 2, 2);
                        
                        """);

                stmt.execute("""
                            INSERT INTO test.orders (order_id, order_date, status, delivery_date, drawing_id, carport_id, customer_id)
                            VALUES (3, '2024-01-05 09:15:00', 'AFSENDT', '2024-02-01 08:00:00', 3, 3, 3);
                        """);

                stmt.execute("SELECT setval('test.orders_order_id_seq', 3, true)");
            }
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
        LocalDateTime delivery = LocalDateTime.of(2026, 2, 10, 0, 0);
        LocalDateTime orderDate = LocalDateTime.of(2025, 2, 3, 0, 0);
        Order order = orderMapper.createOrder(orderDate, "Status", delivery, 1, 1, 1);

        assertEquals(order, orderMapper.getOrderById(4));
    }

    @Test
    void getAllOrders() throws DatabaseException
    {
        List<Order> orders = orderMapper.getAllOrders();

        assertEquals(orderMapper.getOrderById(1), orders.get(0));
        assertEquals(orderMapper.getOrderById(2), orders.get(1));
        assertEquals(orderMapper.getOrderById(3), orders.get(2));
    }

    @Test
    void updateOrderStatus() throws DatabaseException
    {
        assertTrue(orderMapper.updateOrderStatus(1, "GODKENDT"));

    }

    @Test
    void updateOrderDeliveryDate() throws DatabaseException
    {
        LocalDateTime delivery = LocalDateTime.of(2026, 2, 10, 0, 0);
        assertTrue(orderMapper.updateOrderDeliveryDate(3, delivery));
    }

    @Test
    void deleteOrder() throws DatabaseException
    {
        assertTrue(orderMapper.deleteOrder(3));
        assertThrows(DatabaseException.class,
                () -> orderMapper.getOrderById(3));
    }
}