package app.services;

import app.entities.Customer;
import app.exceptions.DatabaseException;

public interface CustomerService {

    public Customer registerNewCustomer(String firstName, String lastName, String email, String phoneNumber, String street, String houseNumber, int zipcode, String city) throws DatabaseException;

    public Customer findCustomerByName(String name) throws DatabaseException;

    public Customer findCustomerById(int id) throws DatabaseException;

    public Customer findCustomerByEmail(String email) throws DatabaseException;

    public Customer findCustomerByPhone(String phone) throws DatabaseException;

    void deleteCustomer(int customerId) throws DatabaseException;
}
