package app.services;

import app.entities.Carport;
import app.exceptions.DatabaseException;

public interface CarportService
{
    Carport getCarportById(int id) throws DatabaseException;

    Carport createCarport(double width, double length, double height, boolean withShed, double shedWidth, double shedLength, String customerWishes) throws DatabaseException;

    void deleteCarport(int id) throws DatabaseException;

    void updateCarport(Carport carport) throws DatabaseException;

    double validateShedMeasurement(double carportWidth, double shedWidth);

    boolean validateShedTotalSize(double carportLength, double carportWidth, double shedLength, double shedWidth);
}

