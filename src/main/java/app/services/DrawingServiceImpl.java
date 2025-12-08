package app.services;

import app.entities.Carport;
import app.entities.Drawing;
import app.exceptions.DatabaseException;
import app.persistence.DrawingMapper;

import java.util.Locale;

public class DrawingServiceImpl implements DrawingService
{
    private DrawingMapper drawingMapper;

    public DrawingServiceImpl(DrawingMapper drawingMapper)
    {
        this.drawingMapper = drawingMapper;
    }

    @Override
    public String showDrawing(Carport carport, CalculatorService calculatorService)
    {
        SvgService svgService = new SvgServiceImpl(0, 0, String.format(Locale.US,"0 0 %.1f %.1f", (carport.getLength()), carport.getWidth()), "100%", "auto");

        CarportTopViewSvg carportSvg = new CarportTopViewSvg(carport, calculatorService, svgService);

        return carportSvg.createMeasuredCarportSvg();
    }

    @Override
    public Drawing createDrawing(Drawing drawing) throws DatabaseException
    {
        return drawingMapper.createDrawing(drawing.getDrawingData());
    }

    @Override
    public void deleteDrawing(int drawingId) throws DatabaseException
    {
        drawingMapper.deleteDrawing(drawingId);
    }
}
