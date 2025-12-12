package app.services;

import app.entities.Customer;
import app.exceptions.DatabaseException;

public interface CustomerService {

    Customer registerNewCustomer(String firstName, String lastName, String email, String phoneNumber, String street, String houseNumber, int zipcode, String city) throws DatabaseException;

    Customer findCustomerByName(String name) throws DatabaseException;

    Customer findCustomerById(int id) throws DatabaseException;

    Customer findCustomerByEmail(String email) throws DatabaseException;

    Customer findCustomerByPhone(String phone) throws DatabaseException;

    void deleteCustomer(int customerId) throws DatabaseException;
}
