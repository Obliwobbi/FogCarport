package app.services;

import app.entities.Carport;
import app.exceptions.DatabaseException;
import app.persistence.CarportMapper;
import app.util.Constants;

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
    public Carport validateAndBuildCarport(Carport carport, double width, double length, double height, boolean withShed, Double shedWidth, Double shedLength, String customerWishes)
    {
        //validate carport dimensions
        double validatedWidth = validateMeasurementInput(width, Constants.MIN_CARPORT_WIDTH, Constants.MAX_CARPORT_WIDTH);
        double validatedLength = validateMeasurementInput(length, Constants.MIN_CARPORT_LENGTH, Constants.MAX_CARPORT_LENGTH);

        Double validatedShedWidth = null;
        Double validatedShedLength = null;

        if (withShed && shedWidth != null && shedLength != null)
        {
            //validate individual shed dimensions
            validatedShedWidth = validateMeasurementInput(shedWidth, Constants.MIN_SHED_WIDTH, Constants.MAX_SHED_WIDTH);
            validatedShedLength = validateMeasurementInput(shedLength, Constants.MIN_SHED_LENGTH, Constants.MAX_SHED_LENGTH);

            //validate against carport size
            validatedShedWidth = validateShedMeasurement(validatedWidth, validatedShedWidth);
            validatedShedLength = validateShedMeasurement(validatedLength, validatedShedLength);

            //validate total shed size (car space)
            validateShedTotalSize(validatedLength, validatedWidth, validatedShedLength, validatedShedWidth);
        }

        String validatedWishes = validateStringInput(customerWishes);

        //create new carport
        if (carport == null)
        {
            return new Carport(validatedWidth, validatedLength, height, withShed,
                    validatedShedWidth, validatedShedLength, validatedWishes);
        }

        //update existing carport
        carport.setWidth(validatedWidth);
        carport.setLength(validatedLength);
        carport.setHeight(height);
        carport.setWithShed(withShed);
        carport.setShedWidth(validatedShedWidth);
        carport.setShedLength(validatedShedLength);
        carport.setCustomerWishes(validatedWishes);

        return carport;
    }

    public Double parseDouble(String value)
    {
        return (value != null && !value.isEmpty()) ? Double.parseDouble(value) : null;
    }

    private double validateShedMeasurement(double carportMeasurement, double shedMeasurement)
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

    private void validateShedTotalSize(double carportLength, double carportWidth, double shedLength, double shedWidth)
    {
        double usableCarportWidth = (carportWidth >= SMALL_MEDIUM_CARPORT) ? carportWidth - OVERHANG_LARGE : carportWidth - OVERHANG_SMALL;

        double remainingCarportLength = carportLength - shedLength;
        double remainingCarportWidth = usableCarportWidth - shedWidth;

        double minCarSpace = 240;

        // Check if space IN FRONT of shed can fit 240x240
        boolean hasSpaceInFront = remainingCarportLength >= minCarSpace && usableCarportWidth >= minCarSpace;

        // Check if space BESIDE shed can fit 240x240
        boolean hasSpaceBeside = remainingCarportWidth >= minCarSpace && carportLength >= minCarSpace;

        if (!hasSpaceInFront && !hasSpaceBeside)
        {
            double maxShedLengthForFront = carportLength - minCarSpace;
            double maxShedWidthForSide = usableCarportWidth - minCarSpace;

            String message = "Der er ikke plads til bilen med nuværende skur mål (minimum 240x240 cm kræves).";

            if (remainingCarportLength < minCarSpace && remainingCarportWidth < minCarSpace)
            {
                message += String.format(" Skuret er for stort i begge retninger. Maksimal skur længde: %.0f cm. Maksimal skur bredde: %.0f cm.",
                        maxShedLengthForFront, maxShedWidthForSide);
            }
            else if (remainingCarportLength < minCarSpace)
            {
                message += String.format(" Manglende plads foran skuret. Maksimal skur længde: %.0f cm.", maxShedLengthForFront);
            }
            else
            {
                message += String.format(" Manglende plads ved siden af skuret. Maksimal skur bredde: %.0f cm.", maxShedWidthForSide);
            }

            throw new IllegalArgumentException(message);
        }
    }

    private double validateMeasurementInput(double input, double min, double max)
    {
        double interval = Constants.CARPORT_MEASUREMENT_INTERVAL;

        if (input < min)
        {
            throw new IllegalArgumentException(input + " cm er under minimumsmål: " + min + " cm");
        }

        int stepsFromMin = (int) ((input - min) / interval);

        double measurement = min + (stepsFromMin * interval);

        return Math.min(measurement, max);
    }

    private String validateStringInput(String input)
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

