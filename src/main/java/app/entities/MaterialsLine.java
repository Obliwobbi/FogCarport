package app.entities;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialsLine
{
    private int lineId;
    private int quantity;
    private double unitPrice;
    private double linePrice;
    private Material material;

    public MaterialsLine(int quantity, double linePrice, Material material)
    {
        this.quantity = quantity;
        this.linePrice = linePrice;
        this.material = material;
    }

}