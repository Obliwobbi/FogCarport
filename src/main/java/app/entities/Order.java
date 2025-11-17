package app.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
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