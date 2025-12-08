package app.services;

import app.entities.Customer;
import app.exceptions.DatabaseException;
import app.persistence.CustomerMapper;

public class CustomerServiceImpl implements CustomerService
{

    private CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerMapper customerMapper)
    {
        this.customerMapper = customerMapper;
    }

    @Override
    public Customer registerNewCustomer(String firstName, String lastName, String email, String phoneNumber, String street, String houseNumber, int zipcode, String city) throws DatabaseException
    {
        return customerMapper.newCustomer(firstName, lastName, email, phoneNumber, street, houseNumber, zipcode, city);
    }

    @Override
    public Customer findCustomerByName(String name)
    {
        return null;
    }

    @Override
    public Customer findCustomerById(int id) throws DatabaseException
    {
        return customerMapper.getCustomerByID(id);
    }

    @Override
    public Customer findCustomerByEmail(String email)
    {
        return null;
    }

    @Override
    public Customer findCustomerByPhone(String phone)
    {
        return null;
    }

    @Override
    public void deleteCustomer(int customerId) throws DatabaseException
    {
        customerMapper.deleteCustomer(customerId);
    }

    @Override
    public String validateEmail(String email)
    {
        if (email == null || email.trim().isEmpty())
        {
            throw new IllegalArgumentException("Email kan ikke være tomt");
        }

        email = email.trim().toLowerCase();

        if (email.length() > 100)
        {
            throw new IllegalArgumentException("Email er for lang");
        }

        if (!email.matches("^[A-Za-z0-9][A-Za-z0-9+._-]*[A-Za-z0-9]@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$"))
        {
            throw new IllegalArgumentException("Ikke gyldig email format");
        }

        return email;
    }
}
