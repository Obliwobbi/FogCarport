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
    public Double validateShedWidth(double carportWidth, double shedWidth)
    {
        return Math.min(carportWidth, shedWidth);
    }
}

