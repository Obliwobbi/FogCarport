package app.entities;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee
{
    private int employeeId;
    private String name;
    private String email;
    private String password;
    private String phone;

    public Employee(int employeeId, String name, String email, String phone)
    {
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}

