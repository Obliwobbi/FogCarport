package app.services;

import app.entities.Employee;
import app.exceptions.DatabaseException;

import java.util.List;

public interface EmployeeService
{
    Employee authenticateEmployee(String email, String password) throws DatabaseException;

    List<Employee> getAllEmployees() throws DatabaseException;
}