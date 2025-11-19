package app.entities;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order
{
    private int orderId;
    private LocalDateTime orderDate;
    private String status; //AFVENTER ACCEPT, ACCEPTERET, BETALT
    private LocalDate deliveryDate;
    private Drawing drawing;
    private Carport carport;
    private BillOfMaterials billOfMaterials;

}