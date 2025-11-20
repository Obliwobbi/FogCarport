package app.persistence;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class MaterialMapperTest
{

    private final static String USER = "postgres";
    private static final String PASSWORD = "ModigsteFryser47";
    private static final String URL = "jdbc:postgresql://164.92.247.68:5432/%s?currentSchema=test";
    private static final String DB = "fogcarport";

    private static ConnectionPool connectionPool;
    private static MaterialMapper materialMapper;

    @BeforeAll
    public static void setUpClass()
    {
        connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, "fogcarport");
        materialMapper = new MaterialMapper(connectionPool);

        try (Connection testConnection = connectionPool.getConnection())
        {
            try (Statement stmt = testConnection.createStatement())
            {

                stmt.execute("DROP TABLE IF EXISTS test.materials CASCADE");

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
    }

    @DisplayName ("Find material in DB by ID")
    @Test
    void getMaterialById()
    {
    }

    @DisplayName("Find material in DB by NAME")
    @Test
    void getMaterialByName()
    {
    }
}