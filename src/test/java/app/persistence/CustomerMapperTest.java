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
    private static ConnectionPool connectionPool;
    private CustomerMapper customerMapper;

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
    void setUp() throws SQLException
    {
        customerMapper = new CustomerMapper(connectionPool);

        // Clean the customers table before each test
        try (Connection connection = connectionPool.getConnection();
             Statement stmt = connection.createStatement())
        {
            stmt.execute("SET search_path TO test");
            stmt.execute("TRUNCATE TABLE customers RESTART IDENTITY CASCADE");
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
        assertThrows(DatabaseException.class, () -> {
            customerMapper.newCustomer(
                    "Jane", "Smith", "duplicate@test.com", "87654321",
                    "Avenue", "2", 2900, "Hellerup"
            );
        });
    }
}
