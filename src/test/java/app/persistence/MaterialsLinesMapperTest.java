package app.persistence;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class MaterialsLinesMapperTest
{
    private final static String USER = "postgres";
    private static final String PASSWORD = "ModigsteFryser47";
    private static final String URL = "jdbc:postgresql://164.92.247.68:5432/%s?currentSchema=test";
    private static final String DB = "fogcarport";

    private static ConnectionPool connectionPool;
    private static MaterialsLinesMapper  materialsLinesMapper;

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

                    // Create Materials Table
                    stmt.execute("""
                                CREATE TABLE materials_lines (
                                    line_id SERIAL PRIMARY KEY,
                                    bom_id INT NOT NULL,
                                    material_id INT NULL,
                                    material_name VARCHAR(100) NOT NULL,
                                    unit_type VARCHAR(50) NOT NULL,
                                    quantity INT NOT NULL,
                                    unit_price DECIMAL(10, 2) NOT NULL,
                                    line_price DECIMAL(10, 2) NOT NULL,
                                    CONSTRAINT fk_bom FOREIGN KEY (bom_id) REFERENCES bills_of_materials (bom_id) ON DELETE CASCADE,
                                    CONSTRAINT fk_material FOREIGN KEY (material_id) REFERENCES materials (id) ON DELETE SET NULL
                                );
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
    }

    @Test
    void createMaterialLine()
    {
    }

    @Test
    void getMaterialLinesByBomId()
    {
    }

    @Test
    void updateMaterialLineName()
    {
    }

    @Test
    void deleteMaterialLine()
    {
    }
}