package app.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BillOfMaterials {
    private int bomId;
    private LocalDateTime createdDate;
    private double totalPrice;
    private List<MaterialsLine> materialLines = new ArrayList<>();

    public BillOfMaterials(int bomId, LocalDateTime createdDate, double totalPrice) {
        this.bomId = bomId;
        this.createdDate = createdDate;
        this.totalPrice = totalPrice;
        this.materialLines = new ArrayList<>();
    }
}

