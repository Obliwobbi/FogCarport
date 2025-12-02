package app.services;

import app.entities.Carport;
import app.exceptions.DatabaseException;
import app.persistence.CarportMapper;

public class CarportServiceImpl implements CarportService
{
    private CarportMapper carportMapper;

    public CarportServiceImpl(CarportMapper carportMapper)
    {
        this.carportMapper = carportMapper;
    }

    @Override
    public Carport getCarportById(int id) throws DatabaseException
    {
        return carportMapper.getCarportById(id);
    }

    @Override
    public Carport createCarport(double width, double length, double height, boolean withShed, double shedWidth, double shedLength, String customerWishes) throws DatabaseException
    {
        return carportMapper.createCarport(width,length,height,withShed,shedWidth,shedLength,customerWishes);
    }

    @Override
    public void deleteCarport(int id) throws DatabaseException
    {
        carportMapper.deleteCarport(id);
    }

    @Override
    public void updateCarport(Carport carport) throws DatabaseException
    {
        carportMapper.updateCarport(carport);
    }

    @Override
    public double validateShedMeasurement(double carportMeasurement, double shedMeasurement)
    {
        return Math.min(carportMeasurement, shedMeasurement);
    }

    @Override
    public boolean validateShedTotalSize(double carportLength, double carportWidth, double shedLength, double shedWidth)
    {
        double remainingCarportLength = carportLength - shedLength;
        double remainingCarportWidth = carportWidth - shedWidth;


        //TODO better validation, needs to account if length is actually long enought that width wont be an issue
        if(remainingCarportLength < 240 || remainingCarportWidth < 240)
        {
            throw new IllegalArgumentException("Der er ikke plads til bilen, med nuværende skur mål, Minimum 240x240 til carport");
        }
        return true;
    }
}

