package app.persistence;

import app.entities.Customer;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class CustomerMapperTest
{
    private final static String USER = "postgres";
    private static final String PASSWORD = "ModigsteFryser47";
    private static final String URL = "jdbc:postgresql://164.92.247.68:5432/%s?currentSchema=test";
    private static final String DB = "fogcarport";

    private static ConnectionPool connectionPool;
    private static CustomerMapper customerMapper;

    @BeforeAll
    public static void setUpClass()
    {
        try
        {
            connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, "fogcarport");
            customerMapper = new CustomerMapper(connectionPool);

            try (Connection testConnection = connectionPool.getConnection())
            {
                try (Statement stmt = testConnection.createStatement())
                {

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
    void setUp() throws SQLException
    {
        customerMapper = new CustomerMapper(connectionPool);

        // Clean the customers table before each test
        try (Connection connection = connectionPool.getConnection();
             Statement stmt = connection.createStatement())
        {
            stmt.execute("SET search_path TO test");
            stmt.execute("TRUNCATE TABLE test.customers RESTART IDENTITY CASCADE");
        }
    }

    @Test
    void newCustomer() throws DatabaseException
    {
        // Arrange
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";
        String phoneNumber = "12345678";
        String street = "Main Street";
        String houseNumber = "42";
        int zipcode = 2800;
        String city = "Lyngby";

        // Act
        Customer customer = customerMapper.newCustomer(
                firstName, lastName, email, phoneNumber,
                street, houseNumber, zipcode, city
        );

        // Assert
        assertNotNull(customer);
        assertTrue(customer.getCustomerId() > 0);
        assertEquals(firstName, customer.getFirstName());
        assertEquals(lastName, customer.getLastName());
        assertEquals(email, customer.getEmail());
        assertEquals(phoneNumber, customer.getPhone());
        assertEquals(street, customer.getStreet());
        assertEquals(houseNumber, customer.getHouseNumber());
        assertEquals(zipcode, customer.getZipcode());
        assertEquals(city, customer.getCity());
    }

    @Test
    void newCustomerWithDuplicateEmail() throws DatabaseException
    {
        // Arrange
        customerMapper.newCustomer(
                "John", "Doe", "duplicate@test.com", "12345678",
                "Street", "1", 2800, "Lyngby"
        );

        // Act & Assert
        assertThrows(DatabaseException.class, () ->
        {
            customerMapper.newCustomer(
                    "Jane", "Smith", "duplicate@test.com", "87654321",
                    "Avenue", "2", 2900, "Hellerup"
            );
        });
    }
}
