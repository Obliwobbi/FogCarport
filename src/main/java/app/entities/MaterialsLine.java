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
public class MaterialsLine {
    private int lineId;
    private int quantity;
    private double unitPrice;
    private Material material;

    public double getTotalPrice() {
        return quantity * unitPrice;
    }
}

