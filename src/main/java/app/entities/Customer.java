package app.entities;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Customer {
    private int customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String street;
    private String houseNumber;
    private int zipcode;
    private String city;
}

