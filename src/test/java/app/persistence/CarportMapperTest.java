package app.persistence;

import app.entities.Carport;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class CarportMapperTest
{
    private final static String USER = "postgres";
    private static final String PASSWORD = "ModigsteFryser47";
    private static final String URL = "jdbc:postgresql://164.92.247.68:5432/%s?currentSchema=test";
    private static final String DB = "fogcarport";

    private static ConnectionPool connectionPool;
    private static CarportMapper carportMapper;


    @BeforeAll
    public static void setUpClass()
    {
        try
        {
            connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, "fogcarport");
            carportMapper = new CarportMapper(connectionPool);

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

                    // Create Employees Table
                    stmt.execute("""
                                CREATE TABLE test.employees (
                                    employee_id SERIAL PRIMARY KEY,
                                    name VARCHAR(100) NOT NULL,
                                    email VARCHAR(100) UNIQUE NOT NULL,
                                    phone VARCHAR(20),
                                    is_admin BOOLEAN DEFAULT FALSE
                                )
                            """);

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
                                    drawing_data TEXT NOT NULL,
                                    accepted BOOLEAN DEFAULT FALSE,
                                    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                                )
                            """);

                    // Create Bills of Materials Table
                    stmt.execute("""
                                CREATE TABLE test.bills_of_materials (
                                    bom_id SERIAL PRIMARY KEY,
                                    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    total_price DECIMAL(12, 2) NOT NULL
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
                                    bom_id INT,
                                    CONSTRAINT fk_drawing FOREIGN KEY (drawing_id) REFERENCES test.drawings(drawing_id) ON DELETE SET NULL,
                                    CONSTRAINT fk_carport FOREIGN KEY (carport_id) REFERENCES test.carports(carport_id) ON DELETE CASCADE,
                                    CONSTRAINT fk_bom FOREIGN KEY (bom_id) REFERENCES test.bills_of_materials(bom_id) ON DELETE SET NULL
                                )
                            """);

                    // Create Materials Lines Table
                    stmt.execute("""
                                CREATE TABLE test.materials_lines (
                                    line_id SERIAL PRIMARY KEY,
                                    bom_id INT NOT NULL,
                                    material_id INT,
                                    material_name VARCHAR(100) NOT NULL,
                                    unit_type VARCHAR(50) NOT NULL,
                                    quantity INT NOT NULL,
                                    unit_price DECIMAL(10, 2) NOT NULL,
                                    line_price DECIMAL(10, 2) NOT NULL,
                                    CONSTRAINT fk_bom FOREIGN KEY (bom_id) REFERENCES test.bills_of_materials(bom_id) ON DELETE CASCADE,
                                    CONSTRAINT fk_material FOREIGN KEY (material_id) REFERENCES test.materials(id) ON DELETE SET NULL
                                )
                            """);
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
                fail("Database connection failed: " + e.getMessage());
            }
        } catch (Exception e)
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

                stmt.execute("DELETE FROM test.carports");

                stmt.execute("""
                            INSERT INTO test.carports (carport_id, width, length, height, with_shed, shed_width, shed_length, customer_wishes)
                            VALUES (1, 600, 780, 210, false, NULL, NULL, 'Standard carport uden skur')
                        """);

                stmt.execute("""
                            INSERT INTO test.carports (carport_id, width, length, height, with_shed, shed_width, shed_length, customer_wishes)
                            VALUES (2, 600, 780, 210, true, 200, 300, 'Carport med skur til haveredskaber')
                        """);

                stmt.execute("""
                            INSERT INTO test.carports (carport_id, width, length, height, with_shed, shed_width, shed_length, customer_wishes)
                            VALUES (3, 500, 600, 200, false, NULL, NULL, NULL)
                        """);

                stmt.execute("SELECT setval('test.carports_carport_id_seq', 3, true)");
            }
        } catch (SQLException e)
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
    void testgetCarportById() throws DatabaseException
    {
        Carport carport = carportMapper.getCarportById(1);

        assertNotNull(carport);
        assertEquals(600, carport.getWidth());
        assertEquals(780, carport.getLength());
        assertEquals(210, carport.getHeight());
        assertFalse(carport.isWithShed());
        assertEquals(0.0, carport.getShedWidth());
        assertEquals(0.0, carport.getShedLength());
        assertEquals("Standard carport uden skur", carport.getCustomerWishes());
    }

    @Test
    void testGetCarportByIdNotFound()
    {
        assertThrows(DatabaseException.class,
                () -> carportMapper.getCarportById(10));
    }

    @Test
    void testcreateCarport() throws DatabaseException
    {
        Carport carport = carportMapper.createCarport(1,2,3,true,4,5,"ingen ønsker");

        assertEquals(carport, carportMapper.getCarportById(4));
    }

    @Test
    void testupdateCarport()
    {
    }

    @Test
    void testdeleteCarport()
    {
    }
}