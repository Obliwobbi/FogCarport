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
public class Carport
{
    private int carportId;
    private double width;
    private double length;
    private double height;
    private boolean withShed;
    private double shedWidth;
    private double shedLength;
    private String customerWishes;

    public Carport(int carportId, double width, double length, double height, boolean withShed, String customerWishes)
    {
        this.carportId = carportId;
        this.width = width;
        this.length = length;
        this.height = height;
        this.withShed = withShed;
        this.customerWishes = customerWishes;
    }
}

