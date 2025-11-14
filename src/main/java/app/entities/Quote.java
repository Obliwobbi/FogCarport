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
public class Quote {
    private int quoteId;
    private double totalPrice;
    private LocalDateTime createdDate;
    private LocalDateTime validUntil;
    private String status;
    private Drawing drawing;
    private Carport carport;
}

