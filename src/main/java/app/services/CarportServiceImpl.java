package app.services;

import app.entities.Carport;
import app.exceptions.DatabaseException;
import app.persistence.CarportMapper;

public class CarportServiceImpl implements CarportService
{
    private CarportMapper carportMapper;
    double SMALL_MEDIUM_CARPORT = 330;
    double OVERHANG_SMALL = 30;
    double OVERHANG_LARGE = 70;

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
    public Carport createCarport(Carport carport) throws DatabaseException
    {
        return carportMapper.createCarport(carport.getWidth(), carport.getLength(), carport.getHeight(), carport.isWithShed(), carport.getShedWidth(), carport.getShedLength(), carport.getCustomerWishes());
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
        double deadZone = 40;

        //creates dead zone so if a shed is to close to the edge it resizes shed to match carport width
        double maxShedSize = (carportMeasurement <= SMALL_MEDIUM_CARPORT) ? carportMeasurement - OVERHANG_SMALL : carportMeasurement - (OVERHANG_SMALL + deadZone);
        double minShedSize = (carportMeasurement <= SMALL_MEDIUM_CARPORT) ? carportMeasurement - OVERHANG_LARGE : carportMeasurement - (OVERHANG_LARGE + deadZone);

        if (shedMeasurement > maxShedSize)
        {
            return maxShedSize;
        }
        else if (shedMeasurement > minShedSize)
        {
            return maxShedSize;
        }
        else
        {
            return shedMeasurement;
        }
    }

    @Override
    public boolean validateShedTotalSize(double carportLength, double carportWidth, double shedLength, double shedWidth)
    {
        double postOffset = (carportWidth >= SMALL_MEDIUM_CARPORT) ? OVERHANG_LARGE / 2 : OVERHANG_SMALL / 2;
        double usableCarportWidth = carportWidth - (2 * postOffset);

        double remainingCarportLength = carportLength - shedLength;
        double remainingCarportWidth = usableCarportWidth - shedWidth;

        double minCarSpace = 240;

        // Check if space IN FRONT of shed can fit 240x240
        boolean hasSpaceInFront = remainingCarportLength >= minCarSpace && usableCarportWidth >= minCarSpace;

        // Check if space BESIDE shed can fit 240x240
        boolean hasSpaceBeside = remainingCarportWidth >= minCarSpace && carportLength >= minCarSpace;

        if (!hasSpaceInFront && !hasSpaceBeside)
        {
            throw new IllegalArgumentException("Der er ikke plads til bilen med nuværende skur mål. Minimum 240x240 cm til carport.");
        }
        return true;
    }

    @Override
    public double validateMeasurementInput(double input, double min, double max)
    {
        double interval = Constants.CARPORT_MEASUREMENT_INTERVAL;

        if (input < min)
        {
            throw new IllegalArgumentException(input + " cm er under minimumsmål: " + min + " cm");
        }

        int stepsFromMin = (int) ((input - min) / interval);

        double measurement = min + (stepsFromMin * interval);

        return Math.min(measurement,max);
    }

    @Override
    public String validateStringInput(String input)
    {
        if (input == null || input.trim().isEmpty())
        {
            return "";
        }

        if (input.length() > 250)
        {
            throw new IllegalArgumentException("Teksten må maksimalt være 250 tegn. Nuværende længde: " + input.length());
        }

        return input;
    }
}

