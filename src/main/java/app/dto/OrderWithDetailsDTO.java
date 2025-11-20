package app.dto;

import app.entities.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderWithDetailsDTO {
    private int orderId;
    private LocalDateTime orderDate;
    private String status;
    private LocalDateTime deliveryDate;
    private Drawing drawing;
    private Carport carport;
    private BillOfMaterials billOfMaterials;
    private Customer customer;
}
