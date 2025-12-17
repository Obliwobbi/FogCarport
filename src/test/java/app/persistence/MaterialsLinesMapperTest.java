package app.persistence;

import app.entities.Material;
import app.entities.MaterialsLine;
import app.entities.Order;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaterialsLinesMapperTest
{
    private final static String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
    private static final String DB = "fogcarport";

    private static ConnectionPool connectionPool;
    private static MaterialsLinesMapper materialsLinesMapper;

    @BeforeAll
    public static void setUpClass()
    {
        try
        {
            connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
            materialsLinesMapper = new MaterialsLinesMapper(connectionPool);

            try (Connection testConnection = connectionPool.getConnection())
            {
                try (Statement stmt = testConnection.createStatement())
                {
                    stmt.execute("DROP TABLE IF EXISTS test.materials_lines CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.employees CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.orders CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.drawings CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.carports CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.materials CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.customers CASCADE");

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

                    stmt.execute("""
                            CREATE TABLE test.employees (
                                employee_id SERIAL PRIMARY KEY,
                                name        VARCHAR(100)        NOT NULL,
                                email       VARCHAR(100) UNIQUE NOT NULL,
                                phone       VARCHAR(20)
                            );
                            """);

                    // Create Materials Table
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
                                    drawing_data TEXT NOT NULL
                                )
                            """);

                    // Create Orders Table
                    stmt.execute("""
                                CREATE TABLE test.orders (
                                    order_id SERIAL PRIMARY KEY,
                                    order_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                                    status VARCHAR(50) NOT NULL DEFAULT 'NY ORDRE',
                                    delivery_date TIMESTAMP WITH TIME ZONE,
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

                    // Create Materials Lines Table
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
                stmt.execute("DELETE FROM test.materials_lines");
                stmt.execute("DELETE FROM test.orders");
                stmt.execute("DELETE FROM test.employees");
                stmt.execute("DELETE FROM test.drawings");
                stmt.execute("DELETE FROM test.carports");
                stmt.execute("DELETE FROM test.materials");
                stmt.execute("DELETE FROM test.customers");


                stmt.execute("ALTER SEQUENCE test.employees_employee_id_seq RESTART WITH 1");

                // Insert test Employees
                stmt.execute("""
                            INSERT INTO test.employees (name, email, phone)
                            VALUES ('Jesper Person', 'jp@fogcarport.dk','+45 23456789'),
                                   ('Toby Person', 'tp@fogcarport.dk','+45 23456790')
                        """);

                // Insert test customers
                stmt.execute("""
                        INSERT INTO test.customers (customer_id, firstname, lastname, email, phone, street, house_number, zipcode, city)
                        VALUES (1, 'Anders', 'Andersen', 'anders@example.com', '+45 12345678', 'Hovedgaden', '10', 2000, 'Frederiksberg')
                        """);

                // Insert test materials
                stmt.execute("""
                        INSERT INTO test.materials (id, name, description, unit, unit_type, material_length, material_width, material_height, price)
                        VALUES (1, '25x200 mm trykimpr. Brædt', 'Understernbrædder', 1, 'stk', 360.00, 20.00, 2.50, 79.00)
                        """);

                stmt.execute("""
                        INSERT INTO test.materials (id, name, description, unit, unit_type, material_length, material_width, material_height, price)
                        VALUES (2, '25x200 mm trykimpr. Brædt', 'Understernbrædder', 1, 'stk', 540.00, 20.00, 2.50, 118.00)
                        """);

                stmt.execute("""
                        INSERT INTO test.materials (id, name, description, unit, unit_type, material_length, material_width, material_height, price)
                        VALUES (3, 'Plastmo bundskruer', 'Skruer til tagplader', 200, 'pakke(r)', NULL, NULL, NULL, 129.00)
                        """);

                // Insert test carports
                stmt.execute("""
                        INSERT INTO test.carports (carport_id, width, length, height, with_shed, shed_width, shed_length, customer_wishes)
                        VALUES (1, 600, 780, 210, FALSE, NULL, NULL, 'Standard carport uden skur')
                        """);

                // Insert test drawings
                stmt.execute("""
                        INSERT INTO test.drawings (drawing_id, drawing_data)
                        VALUES (1, 'SVG drawing data for standard carport 600x780...')
                        """);

                // Insert test orders
                stmt.execute("""
                        INSERT INTO test.orders (order_id, order_date, status, delivery_date, drawing_id, carport_id, customer_id, total_price, employee_id)
                        VALUES (1, '2024-01-15 10:30:00', 'PENDING', '2024-02-15 10:00:00', 1, 1, 1, 0.00,1)
                        """);

                stmt.execute("""
                        INSERT INTO test.orders (order_id, order_date, status, delivery_date, drawing_id, carport_id, customer_id, total_price, employee_id)
                        VALUES (2, '2024-01-10 14:20:00', 'PAID', '2024-02-10 12:00:00', 1, 1, 1, 6484.00,2)
                        """);

                // Insert test materials lines
                stmt.execute("""
                        INSERT INTO test.materials_lines (line_id, order_id, material_id, material_name, unit_type, quantity, unit_price, line_price)
                        VALUES (1, 1, 1, '25x200 mm trykimpr. Brædt', 'stk', 4, 79.00, 316.00)
                        """);

                stmt.execute("""
                        INSERT INTO test.materials_lines (line_id, order_id, material_id, material_name, unit_type, quantity, unit_price, line_price)
                        VALUES (2, 2, 2, '25x200 mm trykimpr. Brædt', 'stk', 2, 118.00, 236.00)
                        """);

                stmt.execute("""
                        INSERT INTO test.materials_lines (line_id, order_id, material_id, material_name, unit_type, quantity, unit_price, line_price)
                        VALUES (3, 2, 3, 'Plastmo bundskruer', 'pakke(r)', 1, 129.00, 129.00)
                        """);

                // Reset sequences
                stmt.execute("SELECT setval('test.customers_customer_id_seq', 1, true)");
                stmt.execute("SELECT setval('test.employees_employee_id_seq', 3, true)");
                stmt.execute("SELECT setval('test.materials_id_seq', 3, true)");
                stmt.execute("SELECT setval('test.carports_carport_id_seq', 1, true)");
                stmt.execute("SELECT setval('test.drawings_drawing_id_seq', 1, true)");
                stmt.execute("SELECT setval('test.orders_order_id_seq', 2, true)");
                stmt.execute("SELECT setval('test.materials_lines_line_id_seq', 3, true)");
            }
        }
        catch (SQLException e)
        {
            fail("Failed to insert test data: " + e.getMessage());
        }
    }

    @Test
    void createMaterialLine() throws DatabaseException
    {
        // Arrange
        int orderId = 1;
        Material material = new Material(2, "25x200 mm trykimpr. Brædt", "Understernbrædder", 1, "stk", 540.00, 20.00, 2.50, 118.00);
        MaterialsLine line = new MaterialsLine(0, 10, 118.0, 1180.00, material);

        // Act
        materialsLinesMapper.createMaterialLine(orderId, line);

        // Assert
        assertTrue(line.getLineId() > 0, "LineId skal være sat efter oprettelse");
        assertEquals(4, line.getLineId(), "Næste line_id skal være 4");
    }

    @Test
    void getMaterialLinesByBomId() throws DatabaseException
    {
        OrderMapper orderMapper = new OrderMapper(connectionPool);
        Order order = orderMapper.getOrderById(2);

        assertNotNull(order);

        List<MaterialsLine> materialsLineList = materialsLinesMapper.getMaterialLinesByOrderId(order.getOrderId());
        assertNotNull(materialsLineList);
    }

    @Test
    void updateMaterialLineName() throws DatabaseException
    {
        OrderMapper orderMapper = new OrderMapper(connectionPool);
        Order order = orderMapper.getOrderById(2);
        assertNotNull(order);

        MaterialsLinesMapper materialsLinesMapper = new MaterialsLinesMapper(connectionPool);
        List<MaterialsLine> materialsLinesList = materialsLinesMapper.getMaterialLinesByOrderId(order.getOrderId());
        MaterialsLine materialsLine = materialsLinesList.get(0);

        String newName = "Nyt test navn";

        assertTrue(materialsLinesMapper.updateMaterialLineName(order.getOrderId(), materialsLine, newName));
        String testNewName = materialsLinesMapper.getMaterialLineName(order.getOrderId(), materialsLine);

        assertEquals(newName, testNewName);
    }

    @Test
    void deleteMaterialLine() throws DatabaseException
    {
        OrderMapper orderMapper = new OrderMapper(connectionPool);
        Order order = orderMapper.getOrderById(2);
        assertNotNull(order);

        MaterialsLinesMapper materialsLinesMapper = new MaterialsLinesMapper(connectionPool);

        List<MaterialsLine> materialsLinesList = materialsLinesMapper.getMaterialLinesByOrderId(order.getOrderId());
        MaterialsLine materialsLine = materialsLinesList.get(0);

        assertTrue(materialsLinesMapper.deleteMaterialLine(materialsLine));

        materialsLinesList = materialsLinesMapper.getMaterialLinesByOrderId(order.getOrderId());

        assertEquals(1, materialsLinesList.size());
    }
}