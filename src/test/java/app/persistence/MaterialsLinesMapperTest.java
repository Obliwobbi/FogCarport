package app.persistence;

import app.entities.Material;
import app.entities.MaterialsLine;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class MaterialsLinesMapperTest
{
    private final static String USER = "postgres";
    private static final String PASSWORD = "ModigsteFryser47";
    private static final String URL = "jdbc:postgresql://164.92.247.68:5432/%s?currentSchema=test";
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
                    stmt.execute("DROP TABLE IF EXISTS test.bills_of_materials CASCADE");
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

                    // Create Materials Table FIRST
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

                    // Create Bills of Materials Table
                    stmt.execute("""
                                CREATE TABLE test.bills_of_materials (
                                    bom_id SERIAL PRIMARY KEY,
                                    total_price DECIMAL(12, 2) NOT NULL
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

                stmt.execute("DELETE FROM test.materials_lines");
                stmt.execute("DELETE FROM test.bills_of_materials");
                stmt.execute("DELETE FROM test.materials");

                stmt.execute("""
                        INSERT INTO test.materials (id, name, description, unit, unit_type, material_length, material_width, material_height, price)
                        VALUES (1, 'Brædt 25x200', '25x200 mm. trykimp. Brædt', 1, 'stk', 540.00, 20.00, 2.50, 300.00)
                        """);

                stmt.execute("""
                        INSERT INTO test.materials (id, name, description, unit, unit_type, material_length, material_width, material_height, price)
                        VALUES (2, 'Skruer 4.5x60', '4,5 x 60 mm. skruer 200 stk.', 200, 'pakke', NULL, NULL, NULL, 120.00)
                        """);

                stmt.execute("""
                        INSERT INTO test.materials (id, name, description, unit, unit_type, material_length, material_width, material_height, price)
                        VALUES (3, 'Bundskruer', 'Plastmo bundskruer 200 stk.', 200, 'pakke', NULL, NULL, NULL, 150.00)
                        """);

                // Insert test bills of materials
                stmt.execute("""
                        INSERT INTO test.bills_of_materials (bom_id, total_price)
                        VALUES (1, 1200.00)
                        """);

                stmt.execute("""
                        INSERT INTO test.bills_of_materials (bom_id, total_price)
                        VALUES (2, 450.00)
                        """);

                // Insert test materials lines
                stmt.execute("""
                        INSERT INTO test.materials_lines (line_id, bom_id, material_id, material_name, unit_type, quantity, unit_price, line_price)
                        VALUES (1, 1, 1, 'Brædt 25x200', 'stk', 4, 300.00, 1200.00)
                        """);

                stmt.execute("""
                        INSERT INTO test.materials_lines (line_id, bom_id, material_id, material_name, unit_type, quantity, unit_price, line_price)
                        VALUES (2, 2, 2, 'Skruer 4.5x60', 'pakke', 2, 120.00, 240.00)
                        """);

                stmt.execute("""
                        INSERT INTO test.materials_lines (line_id, bom_id, material_id, material_name, unit_type, quantity, unit_price, line_price)
                        VALUES (3, 2, 3, 'Bundskruer', 'pakke', 1, 150.00, 150.00)
                        """);

                // Reset sequences
                stmt.execute("SELECT setval('test.materials_id_seq', 3, true)");
                stmt.execute("SELECT setval('test.bills_of_materials_bom_id_seq', 2, true)");
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
        int bomId = 1;
        Material material = new Material(2, "Skruer 4.5x60", "4,5 x 60 mm. skruer 200 stk.", 200, "pakke", 0, 0, 0, 120.00);
        MaterialsLine line = new MaterialsLine(0, 10, 1200.00, material);

        // Act
        materialsLinesMapper.createMaterialLine(bomId, line);

        // Assert
        assertTrue(line.getLineId() > 0, "LineId skal være sat efter oprettelse");
        assertEquals(4, line.getLineId(), "Næste line_id skal være 4");
    }

    @Test
    void getMaterialLinesByBomId() throws DatabaseException
    {

    }

    @Test
    void updateMaterialLineName() throws DatabaseException
    {
    }

    @Test
    void deleteMaterialLine() throws DatabaseException
    {
    }
}