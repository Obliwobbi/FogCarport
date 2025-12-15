package app.services;

import app.entities.Employee;
import app.exceptions.DatabaseException;

public interface EmployeeService
{
    Employee authenticateEmployee(String email, String password) throws DatabaseException;
}
