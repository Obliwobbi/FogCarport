package app.entities;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
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