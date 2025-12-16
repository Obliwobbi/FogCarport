package app.services;

import app.entities.Customer;
import app.exceptions.DatabaseException;

public interface CustomerService {

    Customer validateCustomer(Customer customer, String firstName, String lastName, String email, String phoneNumber, String street, String houseNumber, int zipcode, String city) throws DatabaseException;

    void updateCustomerInfo(Customer customer) throws DatabaseException;

    void deleteCustomer(int customerId) throws DatabaseException;
}
