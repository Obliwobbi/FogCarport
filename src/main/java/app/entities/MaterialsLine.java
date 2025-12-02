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
public class MaterialsLine
{
    private int lineId;
    private int quantity;
    private double linePrice;
    private Material material;

    public MaterialsLine(int quantity, double linePrice, Material material)
    {
        this.quantity = quantity;
        this.linePrice = linePrice;
        this.material = material;
    }
}