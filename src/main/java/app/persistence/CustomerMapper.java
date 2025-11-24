package app.persistence;

import app.entities.Carport;
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

    public Customer getCustomerByID(int customerId) throws DatabaseException
    {
        String sql = "SELECT * FROM customers WHERE customer_id= ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("street"),
                            rs.getString("house_number"),
                            rs.getInt("zipcode"),
                            rs.getString("city"));
                }
                throw new DatabaseException("Der blev ikke fundet en customer med id: " + customerId);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Databasefejl ved hentning af data for customer " + e);
        }
    }

    public boolean deleteCustomer(int customerId) throws DatabaseException
    {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, customerId);
            int rowsAffected = ps.executeUpdate();

            if(rowsAffected == 1)
            {
                return true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af Kunde fra Database" + e.getMessage());
        }
        return false;
    }
}
