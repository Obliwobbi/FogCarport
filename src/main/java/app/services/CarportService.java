package app.services;

import app.entities.Carport;
import app.exceptions.DatabaseException;

public interface CarportService
{
    Carport getCarportById(int id) throws DatabaseException;

    Carport createCarport(Carport carport) throws DatabaseException;

    void deleteCarport(int id) throws DatabaseException;

    void updateCarport(Carport carport) throws DatabaseException;

    Carport validateAndBuildCarport(Carport carport, double width, double length, double height, boolean withShed, Double shedWidth, Double shedLength, String customerWishes);

    Double parseOptionalDouble(String value);
}