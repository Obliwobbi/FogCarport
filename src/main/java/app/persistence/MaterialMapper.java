package app.persistence;

import app.entities.Material;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MaterialMapper
{

    private ConnectionPool connectionPool;

    public MaterialMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Material getMaterialById(int materialId) throws DatabaseException
    {
        String sql = "SELECT * FROM materials WHERE id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, materialId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return new Material(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getInt("unit"),
                            rs.getString("unit_type"),
                            rs.getDouble("material_length"),
                            rs.getDouble("material_width"),
                            rs.getDouble("material_height"),
                            rs.getDouble("price")
                    );
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e.getMessage());
        }


        return null;
    }

}