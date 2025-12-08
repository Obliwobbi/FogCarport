package app.services;

import app.entities.Employee;
import app.exceptions.DatabaseException;

import java.util.List;

public interface EmployeeService
{
    List<Employee> getAllEmployees() throws DatabaseException;
}
