package app.persistence;

import app.entities.Drawing;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DrawingMapperTest
{
    private final static String USER = "postgres";
    private static final String PASSWORD = "ModigsteFryser47";
    private static final String URL = "jdbc:postgresql://164.92.247.68:5432/%s?currentSchema=test";
    private static final String DB = "fogcarport";

    private static ConnectionPool connectionPool;
    private static DrawingMapper drawingMapper;

    @BeforeAll
    public static void setUpClass()
    {
        try
        {
            connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
            drawingMapper = new DrawingMapper(connectionPool);

            try (Connection testConnection = connectionPool.getConnection())
            {
                try (Statement stmt = testConnection.createStatement())
                {
                    // Drop dependent tables first
                    stmt.execute("DROP TABLE IF EXISTS test.materials_lines CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.orders CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.drawings CASCADE");

                    // Create Drawings Table
                    stmt.execute("""
                                CREATE TABLE test.drawings (
                                    drawing_id SERIAL PRIMARY KEY,
                                    drawing_data TEXT NOT NULL
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
                stmt.execute("DELETE FROM test.drawings");

                stmt.execute("""
                            INSERT INTO test.drawings (drawing_id, drawing_data)
                            VALUES (1, 'SVG drawing data for standard carport 600x780...')
                        """);

                stmt.execute("""
                            INSERT INTO test.drawings (drawing_id, drawing_data)
                            VALUES (2, 'SVG drawing data for carport with shed 600x600...')
                        """);

                stmt.execute("""
                            INSERT INTO test.drawings (drawing_id, drawing_data)
                            VALUES (3, 'SVG drawing data for large carport 780x600...')
                        """);

                stmt.execute("SELECT setval('test.drawings_drawing_id_seq', 3, true)");
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
    void createDrawing() throws DatabaseException
    {
        String svgData = "New SVG drawing data for test...";
        Drawing drawing = drawingMapper.createDrawing(svgData);

        assertNotNull(drawing);
        assertEquals(4, drawing.getDrawingId());
        assertEquals(svgData, drawing.getDrawingData());
    }

    @Test
    void getDrawingById() throws DatabaseException
    {
        Drawing drawing = drawingMapper.getDrawingById(1);

        assertNotNull(drawing);
        assertEquals(1, drawing.getDrawingId());
        assertEquals("SVG drawing data for standard carport 600x780...", drawing.getDrawingData());
    }

    @Test
    void getDrawingByIdNotFound()
    {
        assertThrows(DatabaseException.class,
                () -> drawingMapper.getDrawingById(999));
    }

    @Test
    void updateDrawing() throws DatabaseException
    {
        Drawing drawing = drawingMapper.getDrawingById(1);
        String updatedSvgData = "Updated SVG drawing data...";
        drawing = new Drawing(drawing.getDrawingId(), updatedSvgData);

        assertTrue(drawingMapper.updateDrawing(drawing));

        Drawing updatedDrawing = drawingMapper.getDrawingById(1);
        assertEquals(updatedSvgData, updatedDrawing.getDrawingData());
    }

    @Test
    void deleteDrawing() throws DatabaseException
    {
        boolean actual = drawingMapper.deleteDrawing(3);

        assertTrue(actual);
        assertThrows(DatabaseException.class, () -> drawingMapper.getDrawingById(3));
    }
}