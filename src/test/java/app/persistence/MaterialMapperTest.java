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
        assertEquals("25x200 mm trykimpr. Brædt", material.getName());
        assertEquals("Understernbrædder", material.getDescription());
        assertEquals(1, material.getUnit());
        assertEquals("stk", material.getUnitType());
        assertEquals(360, material.getMaterialLength());
        assertEquals(20, material.getMaterialWidth());
        assertEquals(2.50, material.getMaterialHeight());
        assertEquals(79.00, material.getPrice());
    }

    @DisplayName("Find material in DB by NAME")
    @Test
    void getMaterialByName() throws DatabaseException
    {
        Material material = materialMapper.getMaterialByName("Plastmo bundskruer");

        assertNotNull(material);
        assertEquals(3, material.getId());
        assertEquals("Skruer til tagplader", material.getDescription());
        assertEquals(200, material.getUnit());
        assertEquals("pakke(r)", material.getUnitType());
        assertEquals(0, material.getMaterialLength());
        assertEquals(0, material.getMaterialWidth());
        assertEquals(0, material.getMaterialHeight());
        assertEquals(129.00, material.getPrice());
    }

    @DisplayName("Fail: Find material by ID")
    @Test
    void getMaterialByIdFail() throws DatabaseException
    {
        Material material = materialMapper.getMaterialById(4);
        assertNull(material);
    }

    @DisplayName("Fail: Find material by Name")
    @Test
    void getMaterialByNameFail() throws DatabaseException
    {
        Material material = materialMapper.getMaterialByName("Skurer");
        assertNull(material);
    }
}