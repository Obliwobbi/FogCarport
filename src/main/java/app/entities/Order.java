package app.entities;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order
{
    private int orderId;
    private LocalDateTime orderDate;
    private String status; //AFVENTER ACCEPT, ACCEPTERET, BETALT
    private LocalDateTime deliveryDate;
    private Drawing drawing;
    private Carport carport;
    private BillOfMaterials billOfMaterials;

}