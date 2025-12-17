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
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=test";
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

                stmt.execute("DELETE FROM test.carports");

                stmt.execute("""
                            INSERT INTO test.carports (carport_id, width, length, height, with_shed, shed_width, shed_length, customer_wishes)
                            VALUES (1, 600, 780, 210, FALSE, NULL, NULL, 'Standard carport uden skur')
                        """);

                stmt.execute("""
                            INSERT INTO test.carports (carport_id, width, length, height, with_shed, shed_width, shed_length, customer_wishes)
                            VALUES (2, 600, 600, 210, TRUE, 300, 210, 'Carport med skur til haveredskaber')
                        """);

                stmt.execute("""
                            INSERT INTO test.carports (carport_id, width, length, height, with_shed, shed_width, shed_length, customer_wishes)
                            VALUES (3, 780, 600, 240, TRUE, 300, 240, 'Stor carport med skur')
                        """);

                stmt.execute("SELECT setval('test.carports_carport_id_seq', 3, true)");
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
        Carport carport = carportMapper.createCarport(1, 2, 3, true, 4, 5, "ingen ønsker");

        assertEquals(carport, carportMapper.getCarportById(4));
    }

    @Test
    void testupdateCarport() throws DatabaseException
    {
        Carport carportEdited = carportMapper.getCarportById(1);
        carportEdited.setHeight(300);

        assertTrue(carportMapper.updateCarport(carportEdited));
        assertEquals(300, carportMapper.getCarportById(1).getHeight());
    }

    @Test
    void testdeleteCarport() throws DatabaseException
    {
        assertTrue(carportMapper.deleteCarport(3));
        assertThrows(DatabaseException.class,
                () -> carportMapper.getCarportById(3));
    }
}