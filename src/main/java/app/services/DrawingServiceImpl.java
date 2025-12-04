package app.services;

import app.entities.Drawing;
import app.exceptions.DatabaseException;
import app.persistence.DrawingMapper;

public class DrawingServiceImpl implements DrawingService
{
    private DrawingMapper drawingMapper;

    public DrawingServiceImpl(DrawingMapper drawingMapper)
    {
        this.drawingMapper = drawingMapper;
    }

    @Override
    public Drawing createDrawing(String svgData) throws DatabaseException
    {
        return drawingMapper.createDrawing(svgData);
    }
}
