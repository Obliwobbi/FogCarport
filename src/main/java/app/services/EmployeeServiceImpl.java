package app.services;

import app.entities.Employee;
import app.exceptions.DatabaseException;
import app.persistence.EmployeeMapper;

import java.util.List;

public class EmployeeServiceImpl implements EmployeeService
{
    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper)
    {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public List<Employee> getAllEmployees() throws DatabaseException
    {
        return employeeMapper.getAllEmployees();
    }
}
