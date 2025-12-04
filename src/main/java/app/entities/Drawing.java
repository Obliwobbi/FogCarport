package app.entities;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drawing {
    private int drawingId;
    private String drawingData;

    public Drawing(String drawingData)
    {
        this.drawingData = drawingData;
    }
}

