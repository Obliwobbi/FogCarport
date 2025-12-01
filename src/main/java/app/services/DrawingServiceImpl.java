package app.services;

import app.entities.Carport;

public class DrawingServiceImpl implements DrawingService
{
    @Override
    public String createViewBox(Carport carport)
    {
        String carportWidth = String.valueOf(carport.getWidth() + 100);
        String carportLength = String.valueOf(carport.getLength() + 100);

        return "0 0 " + carportLength + " " + carportWidth;
    }
}
