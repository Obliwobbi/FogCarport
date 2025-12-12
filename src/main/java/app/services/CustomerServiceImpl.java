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
        String validatedFirstName = validateFirstOrLastName(firstName, "Fornavn");
        String validatedLastName = validateFirstOrLastName(lastName, "Efternavn");
        String validatedEmail = validateEmail(email);
        String validatedPhone = validatePhone(phoneNumber);
        String validatedStreet = validateStreet(street);
        String validatedHouseNumber = validateHouseNumber(houseNumber);
        validateZipCode(zipcode);
        String validatedCity = validateCity(city);

        return customerMapper.newCustomer(validatedFirstName, validatedLastName, validatedEmail,
                validatedPhone, validatedStreet, validatedHouseNumber,
                zipcode, validatedCity);
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

    private String validateEmail(String email)
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

    private String validateFirstOrLastName(String name, String fieldName)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " kan ikke være tomt");
        }

        name = name.trim();

        if (name.length() < 2)
        {
            throw new IllegalArgumentException(fieldName + " skal minimum være 2 tegn");
        }

        if (name.length() > 50)
        {
            throw new IllegalArgumentException(fieldName + " er for langt (max 50 tegn)");
        }


        if (!name.matches("^[a-zA-ZæøåÆØÅ\\s'-]+$"))
        {
            throw new IllegalArgumentException(fieldName + " må kun indeholde bogstaver, mellemrum og bindestreg");
        }

        return name;
    }

    private void validateZipCode(int zipCode)
    {
        if (zipCode < 1000 || zipCode > 9999)
        {
            throw new IllegalArgumentException("Postnummer skal være mellem 1000 og 9999");
        }
    }

    private String validatePhone(String phone)
    {
        if (phone == null || phone.trim().isEmpty())
        {
            throw new IllegalArgumentException("Telefonnummer kan ikke være tomt");
        }

        phone = phone.trim().replaceAll("\\s", "");


        if (!phone.matches("^\\d{8}$"))
        {
            throw new IllegalArgumentException("Telefonnummer skal være 8 cifre");
        }

        return phone;
    }

    private String validateCity(String city)
    {
        if (city == null || city.trim().isEmpty())
        {
            throw new IllegalArgumentException("By kan ikke være tom");
        }

        city = city.trim();

        if (city.length() < 2)
        {
            throw new IllegalArgumentException("By skal være mindst 2 tegn");
        }

        if (city.length() > 50)
        {
            throw new IllegalArgumentException("By er for langt (max 50 tegn)");
        }

        if (!city.matches("^[a-zA-ZæøåÆØÅ\\s'-]+$"))
        {
            throw new IllegalArgumentException("By må kun indeholde bogstaver");
        }

        return city;
    }

    private String validateStreet(String street)
    {
        if (street == null || street.trim().isEmpty())
        {
            throw new IllegalArgumentException("Gade kan ikke være tom");
        }

        street = street.trim();

        if (street.length() < 2)
        {
            throw new IllegalArgumentException("Gade skal være mindst 2 tegn");
        }

        if (street.length() > 100)
        {
            throw new IllegalArgumentException("Gade er for langt (max 100 tegn)");
        }

        if (!street.matches(".*[a-zA-ZæøåÆØÅ].*"))
        {
            throw new IllegalArgumentException("Gade skal indeholde mindst ét bogstav");
        }

        if (!street.matches("^[a-zA-Z0-9æøåÆØÅ\\s.'-]+$"))
        {
            throw new IllegalArgumentException("Gade indeholder ugyldige tegn");
        }

        return street;
    }

    private String validateHouseNumber(String houseNumber)
    {
        if (houseNumber == null || houseNumber.trim().isEmpty())
        {
            throw new IllegalArgumentException("Husnummer kan ikke være tomt");
        }

        houseNumber = houseNumber.trim();

        if (houseNumber.length() > 10)
        {
            throw new IllegalArgumentException("Husnummer er for langt (max 10 tegn)");
        }

        if (!houseNumber.matches("^[0-9]+[A-Za-z]?([\\s/-][0-9]+[A-Za-z]?)?$"))
        {
            throw new IllegalArgumentException("Ugyldigt husnummer format");
        }

        return houseNumber;
    }
}
