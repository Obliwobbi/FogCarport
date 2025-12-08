package app.persistence;

import app.entities.Employee;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeMapper
{

    private ConnectionPool connectionPool;

    public EmployeeMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Employee getEmployeeById(int employeeId) throws DatabaseException
    {
        String sql = "SELECT * FROM employees WHERE employee_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                return new Employee(
                        rs.getInt("employee_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone")
                );
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af material på id:" + e.getMessage());
        }
        return null;
    }
}