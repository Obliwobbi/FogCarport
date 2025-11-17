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
public class Material
{
    private int id;
    private String name;
    private String description;
    private int unit; //1
    private String unitType; //Rulle, stk osv.
    private double materialLength; //20m på en rulle
    private double materialWidth; //2m
    private double materialHeight; //35mm
    private double price;
}