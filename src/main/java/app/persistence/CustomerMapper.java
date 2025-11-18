package app.persistence;

import app.entities.Customer;
import app.exceptions.DatabaseException;

import java.sql.*;

public class CustomerMapper
{

    private ConnectionPool connectionPool;

    public CustomerMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Customer newCustomer(String firstName, String lastName, String email, String phoneNumber, String street, String houseNumber, int zipcode, String city) throws DatabaseException
    {
        String sql = """
                INSERT INTO customers 
                (firstname, lastname, email, phone, street, house_number, zipcode, city) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING customer_id
                """;
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, phoneNumber);
            ps.setString(5, street);
            ps.setString(6, houseNumber);
            ps.setInt(7, zipcode);
            ps.setString(8, city);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected != 1)
            {
                throw new DatabaseException("Fejl ved kunde indlæsning");
            }

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
            {
                int customerId = rs.getInt(1);
                return new Customer(customerId, firstName, lastName, email, phoneNumber, street, houseNumber, zipcode, city);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved kunde: " + e.getMessage());
        }
        return null;
    }

}
