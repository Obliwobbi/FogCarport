package app.services;

import app.entities.Customer;
import app.exceptions.DatabaseException;
import app.persistence.CustomerMapper;

public class CustomerServiceImpl implements CustomerService {

    private CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerMapper customerMapper)
    {
        this.customerMapper = customerMapper;
    }

    @Override
    public Customer registerNewCustomer(String firstName, String lastName, String email, String phoneNumber, String street, String houseNumber, int zipcode, String city) throws DatabaseException
    {
        return customerMapper.newCustomer(firstName,lastName,email,phoneNumber,street,houseNumber,zipcode,city);
    }

    @Override
    public Customer findCustomerByName(String name)
    {
        return null;
    }

    @Override
    public Customer findCustomerById(long id)
    {
        return null;
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
}