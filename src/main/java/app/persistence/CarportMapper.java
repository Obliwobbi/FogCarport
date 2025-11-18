package app.persistence;

import app.entities.Carport;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CarportMapper
{
    private final ConnectionPool connectionPool;

    public CarportMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Carport getCarportById(int carportId) throws DatabaseException
    {
        String sql = "SELECT * FROM carports WHERE carport_id= ?";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, carportId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return new Carport(
                            rs.getInt("carport_id"),
                            rs.getDouble("width"),
                            rs.getDouble("length"),
                            rs.getDouble("height"),
                            rs.getBoolean("with_shed"),
                            rs.getDouble("shed_width"),
                            rs.getDouble("shed_length"),
                            rs.getString("customerWishes")
                    );
                }
                return null;
            }
        } catch (SQLException e)
        {
            throw new DatabaseException("Database error while fetching Carport: " + e);
        }
    }

    public Carport createCarport() throws DatabaseException
    {

    }

    public boolean updateCarport(Carport carport) throws DatabaseException
    {

    }

    public boolean deleteCarport(int carportId) throws DatabaseException
    {

    }


}


