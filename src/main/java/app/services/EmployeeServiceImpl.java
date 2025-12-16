package app.services;

import app.entities.Employee;
import app.exceptions.DatabaseException;
import app.persistence.EmployeeMapper;
import org.mindrot.jbcrypt.BCrypt;

public class EmployeeServiceImpl implements EmployeeService
{
    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper)
    {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public Employee authenticateEmployee(String email, String password) throws DatabaseException
    {
        Employee employee = employeeMapper.getEmployeeByEmail(email);
        if (employee != null && BCrypt.checkpw(password, employee.getPassword()))
        {
            return employee;
        }
        else
        {
            throw new DatabaseException("Forkert email eller password");
        }
    }
}
