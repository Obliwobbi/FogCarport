package app.persistence;

import app.entities.Carport;
import app.exceptions.DatabaseException;

import java.sql.*;


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
                    if (rs.getBoolean("with_shed"))
                    {
                        {
                            return new Carport(
                                    rs.getInt("carport_id"),
                                    rs.getDouble("width"),
                                    rs.getDouble("length"),
                                    rs.getDouble("height"),
                                    rs.getBoolean("with_shed"),
                                    rs.getDouble("shed_width"),
                                    rs.getDouble("shed_length"),
                                    rs.getString("customer_wishes"));
                        }
                    }
                    else
                    {
                        return new Carport(
                                rs.getInt("carport_id"),
                                rs.getDouble("width"),
                                rs.getDouble("length"),
                                rs.getDouble("height"),
                                rs.getBoolean("with_shed"),
                                rs.getString("customer_wishes"));
                    }

                throw new DatabaseException("Der blev ikke fundet en carport med id: " + carportId);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Database ved hentning af data for carport " + e);
        }
    }

    public Carport createCarport(double width, double length, double height, boolean withShed, double shedWidth, double shedLength, String customerWishes) throws DatabaseException
    {
        String sql = "INSERT INTO carports (width, length, height, with_shed, shed_width, shed_length, customer_wishes)" +
                "VALUES(?,?,?,?,?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setDouble(1, width);
            ps.setDouble(2, length);
            ps.setDouble(3, height);
            ps.setBoolean(4, withShed);
            ps.setString(7, customerWishes);

            if (withShed)
            {
                ps.setDouble(5, shedWidth);
                ps.setDouble(6, shedLength);
            }
            else
            {
                ps.setNull(5, Types.NUMERIC);
                ps.setNull(6, Types.NUMERIC);
            }

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected != 1)
            {
                throw new DatabaseException("Uventet fejl");
            }

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next())
            {
                int carportId = rs.getInt(1);

                if (withShed)
                {
                    return new Carport(carportId, width, length, height, withShed, shedWidth, shedLength, customerWishes);
                }
                else
                {
                    return new Carport(carportId, width, length, height, withShed, customerWishes);
                }
            }
        }
        catch (SQLException e)
        {
            if (e.getSQLState().equals("23505")) // error code is the standard for catching unique constraint errors in PostgresSQL
            {
                throw new DatabaseException("carport findes allerede");
            }
            else
            {
                throw new DatabaseException("Databasefejl ved oprettelse af carport " + e.getMessage());
            }
        }
        return null;
    }

    public boolean updateCarport(Carport carport) throws DatabaseException
    {
        String sql = "UPDATE carports SET width = ?, length = ?, height = ?, with_shed = ?, shed_width = ?, shed_length = ?, customer_wishes = ? WHERE carport_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setDouble(1, carport.getWidth());
            ps.setDouble(2, carport.getLength());
            ps.setDouble(3, carport.getHeight());
            ps.setBoolean(4, carport.isWithShed());
            ps.setString(7, carport.getCustomerWishes());
            ps.setInt(8, carport.getCarportId());

            if (carport.isWithShed())
            {
                ps.setDouble(5, carport.getShedWidth());
                ps.setDouble(6, carport.getShedLength());
            }
            else
            {
                ps.setNull(5, Types.NUMERIC);
                ps.setNull(6, Types.NUMERIC);
            }
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                return true;
            }
            else
            {
                throw new DatabaseException("Carport blev ikke opdateret - id: " + carport.getCarportId());
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af carport: " + e.getMessage());
        }
    }

    public boolean deleteCarport(int carportId) throws DatabaseException
    {
        String sql = "DELETE FROM carports WHERE carport_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, carportId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                return true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af carport med id: " + carportId);
        }
        return false;
    }
}





