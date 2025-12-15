package app.dto;

import app.entities.*;
import app.util.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderWithDetailsDTO {
    private int orderId;
    private LocalDateTime orderDate;
    private Status status;
    private LocalDateTime deliveryDate;
    private Drawing drawing;
    private List<MaterialsLine> materialsLines;
    private Carport carport;
    private Customer customer;
    private Employee employee;

    public double getTotalPrice() {
        if (materialsLines == null || materialsLines.isEmpty()) {
            return 0.0;
        }
        return materialsLines.stream()
                .mapToDouble(MaterialsLine::getLinePrice)
                .sum();
    }
}
