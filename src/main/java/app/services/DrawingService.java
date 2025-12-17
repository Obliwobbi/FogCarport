package app.services;

import app.entities.Carport;
import app.entities.Drawing;
import app.exceptions.DatabaseException;

public interface DrawingService
{
    Drawing createDrawing(Drawing drawing) throws DatabaseException;

    void updateDrawing(Drawing drawing) throws DatabaseException;

    void deleteDrawing(int drawingId) throws DatabaseException;

    String showDrawing(Carport carport, CalculatorService calculatorService);
}