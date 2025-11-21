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
    private Integer drawingId;
    private int carportId;
    private List<MaterialsLine> materialLines;
    private int customerId;
}