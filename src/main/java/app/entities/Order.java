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
public class Order {
    private int orderId;
    private LocalDateTime orderDate;
    private String status;
    private LocalDateTime deliveryDate;
    private BillOfMaterials billOfMaterials;
    private Quote quote;

    public Order(int orderId, LocalDateTime orderDate, String status, LocalDateTime deliveryDate) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.status = status;
        this.deliveryDate = deliveryDate;
    }
}

