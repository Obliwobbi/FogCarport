package app.persistence;

import app.entities.Drawing;
import app.exceptions.DatabaseException;

import java.sql.*;

public class DrawingMapper
{
    private ConnectionPool connectionPool;

    public DrawingMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Drawing createDrawing(String svgData) throws DatabaseException
    {
        String sql = "INSERT INTO drawings (drawing_data)" +
                " VALUES (?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, svgData);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected != 1)
            {
                throw new DatabaseException("Uventet fejl");
            }

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next())
            {
                int drawingId = rs.getInt(1);
                return new Drawing(drawingId, svgData);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Database fejl ved opretetelse af Tegning" + e.getMessage());
        }
        return null;
    }

    public Drawing getDrawingById(int drawingId) throws DatabaseException
    {
        String sql = "SELECT * FROM drawings WHERE drawing_id= ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, drawingId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return new Drawing(
                            rs.getInt("drawing_id"),
                            rs.getString("drawing_data"));
                }
                throw new DatabaseException("der blev ikke fundet en tegning med id: " + drawingId);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af data for tegning" + e);
        }
    }

    public boolean updateDrawing(Drawing drawing) throws DatabaseException
    {
        String sql = "UPDATE drawings SET drawing_data = ? WHERE drawing_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, drawing.getDrawingData());
            ps.setInt(2, drawing.getDrawingId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 1)
            {
                return true;
            }
            else
            {
                throw new DatabaseException("Tegning blev ikke opdateret - id: " + drawing.getDrawingId());
            }

        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af tegning: " + e.getMessage());
        }
    }

    public boolean deleteDrawing(int drawingId) throws DatabaseException
    {
        String sql = "DELETE FROM drawings WHERE drawing_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, drawingId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                return true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af Tegning med id: " + drawingId);
        }
        return false;
    }
}

