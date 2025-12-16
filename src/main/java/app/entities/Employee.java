package app.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Employee {
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

