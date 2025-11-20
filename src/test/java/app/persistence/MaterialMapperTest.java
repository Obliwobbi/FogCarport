package app.persistence;

import app.entities.Material;
import app.exceptions.DatabaseException;
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
        connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
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
        try (Connection connection = connectionPool.getConnection())
        {
            try (Statement stmt = connection.createStatement())
            {

                stmt.execute("DELETE FROM test.materials");

                stmt.execute("""
                        INSERT INTO materials (id, name, description, unit, unit_type, material_length, material_width, material_height, price)
                        VALUES (1, 'Brædt 25x200', '25x200 mm. trykimp. Brædt', 1, 'stk', 540.00, 20.00, 2.50, 300.00)
                        """);

                stmt.execute("""
                        INSERT INTO materials (id, name, description, unit, unit_type, material_length, material_width, material_height, price)
                        VALUES (2, 'Skruer 4.5x60', '4,5 x 60 mm. skruer 200 stk.', 200, 'pakke', NULL, NULL, NULL, 120.00)
                        """);

                stmt.execute("""
                        INSERT INTO materials (id, name, description, unit, unit_type, material_length, material_width, material_height, price)
                        VALUES (3, 'Bundskruer', 'Plastmo bundskruer 200 stk.', 200, 'pakke', NULL, NULL, NULL, 150.00)
                        """);

                stmt.execute("SELECT setval('test.materials_id_seq', 3, true)");
            }
        }
        catch (SQLException e)
        {
            fail("Failed to insert test data: " + e.getMessage());
        }
    }

    @DisplayName("Find material in DB by ID")
    @Test
    void getMaterialById() throws DatabaseException
    {
        Material material = materialMapper.getMaterialById(1);

        assertNotNull(material);
        assertEquals("Brædt 25x200", material.getName());
        assertEquals("25x200 mm. trykimp. Brædt", material.getDescription());
        assertEquals(1, material.getUnit());
        assertEquals("stk", material.getUnitType());
        assertEquals(540, material.getMaterialLength());
        assertEquals(20, material.getMaterialWidth());
        assertEquals(2.50, material.getMaterialHeight());
        assertEquals(300, material.getPrice());
    }

    @DisplayName("Find material in DB by NAME")
    @Test
    void getMaterialByName() throws DatabaseException
    {
        Material material = materialMapper.getMaterialByName("Bundskruer");

        assertNotNull(material);
        assertEquals(3, material.getId());
        assertEquals("Plastmo bundskruer 200 stk.", material.getDescription());
        assertEquals(200, material.getUnit());
        assertEquals("pakke", material.getUnitType());
        assertEquals(0, material.getMaterialLength());
        assertEquals(0, material.getMaterialWidth());
        assertEquals(0, material.getMaterialHeight());
        assertEquals(150, material.getPrice());
    }
}