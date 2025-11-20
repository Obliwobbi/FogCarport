package app.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BillOfMaterials
{
    private int bomId;
    private double totalPrice;
    private List<MaterialsLine> materialLines = new ArrayList<>();

    public BillOfMaterials(int bomId, double totalPrice)
    {
        this.bomId = bomId;
        this.totalPrice = totalPrice;
        this.materialLines = new ArrayList<>();
    }
}