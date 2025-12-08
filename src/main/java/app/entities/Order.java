package app.entities;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order
{
    private int orderId;
    private LocalDateTime orderDate;
    private String status; //AFVENTER ACCEPT, ACCEPTERET, BETALT
    private LocalDateTime deliveryDate;
    private int drawingId;
    private int carportId;
    private List<MaterialsLine> materialLines;
    private int customerId;

    public Order(int drawingId, int carportId, int customerId)
    {
        this.carportId = carportId;
        this.drawingId = drawingId;
        this.customerId = customerId;
    }

    public Order(int orderId, int drawingId, int carportId, List<MaterialsLine> materialLines, int customerId)
    {
        this.orderId = orderId;
        this.drawingId = drawingId;
        this.carportId = carportId;
        this.materialLines = materialLines;
        this.customerId = customerId;
    }
}