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
public class Carport {
    private int carportId;
    private double width;
    private double length;
    private double height;
    private boolean withShed;
    private double shedWidth;
    private double shedLength;
}

