package app.services;

import app.entities.Drawing;
import app.exceptions.DatabaseException;

public interface DrawingService
{
    Drawing createDrawing(String svgData) throws DatabaseException;
}
